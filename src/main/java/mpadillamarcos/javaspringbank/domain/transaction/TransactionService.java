package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.account.AccountService;
import mpadillamarcos.javaspringbank.domain.balance.BalanceService;
import mpadillamarcos.javaspringbank.domain.exception.AccessDeniedException;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.exception.TransactionNotAllowedException;
import mpadillamarcos.javaspringbank.domain.time.Clock;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.List;

import static mpadillamarcos.javaspringbank.domain.account.AccountState.OPEN;
import static mpadillamarcos.javaspringbank.domain.transaction.Transaction.newTransaction;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.INCOMING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.OUTGOING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountService accountService;
    private final AccountAccessService accessService;
    private final BalanceService balanceService;
    private final Clock clock;

    public void transfer(TransferRequest transferRequest) {
        var userId = transferRequest.getUserId();
        var originAccountId = transferRequest.getAccountId();
        var destinationAccountId = transferRequest.getDestinationAccountId();
        var amount = transferRequest.getAmount();
        var groupId = randomTransactionGroupId();

        canOperate(originAccountId, userId);
        checkAccountIsOpen(destinationAccountId);
        balanceService.withdraw(originAccountId, amount);

        var outgoingTransaction = newTransaction()
                .groupId(groupId)
                .userId(userId)
                .accountId(originAccountId)
                .amount(amount)
                .createdDate(clock.now())
                .type(TRANSFER)
                .direction(OUTGOING)
                .concept(transferRequest.getConcept())
                .build();

        var incomingTransaction = newTransaction()
                .groupId(groupId)
                .userId(userId)
                .accountId(destinationAccountId)
                .amount(amount)
                .createdDate(clock.now())
                .type(TRANSFER)
                .direction(INCOMING)
                .concept(transferRequest.getConcept())
                .build();

        repository.insert(outgoingTransaction);
        repository.insert(incomingTransaction);
    }

    public void withdraw(WithdrawRequest withdrawRequest) {
        var userId = withdrawRequest.getUserId();
        var accountId = withdrawRequest.getAccountId();

        canOperate(accountId, userId);

        var withdrawTransaction = newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(userId)
                .accountId(accountId)
                .amount(withdrawRequest.getAmount())
                .createdDate(clock.now())
                .type(WITHDRAW)
                .direction(OUTGOING)
                .concept(withdrawRequest.getConcept())
                .build();

        repository.insert(withdrawTransaction);
    }

    public void deposit(DepositRequest depositRequest) {
        var userId = depositRequest.getUserId();
        var accountId = depositRequest.getAccountId();

        canOperate(accountId, userId);

        var depositTransaction = newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(userId)
                .accountId(accountId)
                .amount(depositRequest.getAmount())
                .createdDate(clock.now())
                .type(DEPOSIT)
                .direction(INCOMING)
                .concept(depositRequest.getConcept())
                .build();

        repository.insert(depositTransaction);
    }

    public List<Transaction> listTransactionsByAccountId(AccountId accountId) {
        return repository.findTransactionsByAccountId(accountId);
    }

    public void confirm(TransactionId transactionId) {
        var transaction = getTransactionById(transactionId);
        List<Transaction> transactions = repository.findTransactionsByGroupId(transaction.getGroupId());
        transactions.forEach(this::confirm);
    }

    private void confirm(Transaction transaction) {
        isAccountOpen(transaction);
        repository.update(transaction.confirm());
        if (transaction.is(INCOMING)) {
            balanceService.deposit(transaction.getAccountId(), transaction.getAmount());
        }
        if (transaction.is(OUTGOING) && !transaction.is(TRANSFER)) {
            balanceService.withdraw(transaction.getAccountId(), transaction.getAmount());
        }
    }

    public void reject(TransactionId transactionId) {
        var transaction = getTransactionById(transactionId);
        List<Transaction> transactions = repository.findTransactionsByGroupId(transaction.getGroupId());
        transactions.forEach(this::reject);
    }

    private void reject(Transaction transaction) {
        isAccountOpen(transaction);
        repository.update(transaction.reject());
        if (transaction.is(OUTGOING) && transaction.is(TRANSFER)) {
            balanceService.deposit(transaction.getAccountId(), transaction.getAmount());
        }
    }

    private void canOperate(AccountId accountId, UserId userId) {
        checkAccountIsOpen(accountId);
        var access = accessService.findAccountAccess(accountId, userId)
                .orElseThrow(() -> new AccessDeniedException("User with ID " + userId.value() + " has no access to that account"));

        if (!access.canOperate()) {
            throw new AccessDeniedException("User with ID " + userId.value() + " has no operation permits");
        }
    }

    private void checkAccountIsOpen(AccountId accountId) {
        var account = accountService.getById(accountId);
        if (!account.is(OPEN)) {
            throw new TransactionNotAllowedException("The account with ID " + accountId.value() + " is " + account.getState());
        }
    }

    private void isAccountOpen(Transaction transaction) {
        var account = accountService.getById(transaction.getAccountId());
        if (!account.is(OPEN)) {
            throw new IllegalStateException("The account with ID " + account.getId().value() + " is not open");
        }
    }

    private Transaction getTransactionById(TransactionId transactionId) {
        return repository.findTransactionById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction ID " + transactionId.value() + " not found"));
    }
}
