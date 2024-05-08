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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

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

    @Transactional
    public TransactionId transfer(TransferRequest transferRequest) {
        var userId = transferRequest.getUserId();
        var originAccountId = transferRequest.getOriginAccountId();
        var destinationAccountId = transferRequest.getDestinationAccountId();
        var amount = transferRequest.getAmount();
        var groupId = randomTransactionGroupId();

        canOperate(originAccountId, userId);
        canPlaceTransactionOnAccount(destinationAccountId);
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

        return outgoingTransaction.getId();
    }

    @Transactional
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
        confirm(withdrawTransaction);
    }

    @Transactional
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
        confirm(depositTransaction);
    }

    public List<Transaction> listTransactionsByAccountId(AccountId accountId) {
        return repository.findTransactionsByAccountId(accountId);
    }

    @Transactional
    public void confirm(TransactionId transactionId) {
        var transaction = getTransactionById(transactionId);
        List<Transaction> transactions = repository.findTransactionsByGroupIdForUpdate(transaction.getGroupId());
        transactions.forEach(this::confirm);
    }

    private void confirm(Transaction transaction) {
        canUpdateTransaction(transaction);
        repository.update(transaction.confirm());
        if (transaction.is(INCOMING)) {
            balanceService.deposit(transaction.getAccountId(), transaction.getAmount());
        }
        if (transaction.is(OUTGOING) && !transaction.is(TRANSFER)) {
            balanceService.withdraw(transaction.getAccountId(), transaction.getAmount());
        }
    }

    @Transactional
    public void reject(TransactionId transactionId) {
        var transaction = getTransactionById(transactionId);
        List<Transaction> transactions = repository.findTransactionsByGroupId(transaction.getGroupId());
        transactions.forEach(this::reject);
    }

    private void reject(Transaction transaction) {
        canUpdateTransaction(transaction);
        repository.update(transaction.reject());
        if (transaction.is(OUTGOING) && transaction.is(TRANSFER)) {
            balanceService.deposit(transaction.getAccountId(), transaction.getAmount());
        }
    }

    private void canOperate(AccountId accountId, UserId userId) {
        canPlaceTransactionOnAccount(accountId);
        var access = accessService.findAccountAccess(accountId, userId)
                .orElseThrow(() -> new AccessDeniedException("User with ID " + userId.value() + " has no access to that account"));

        if (!access.canOperate()) {
            throw new AccessDeniedException("User with ID " + userId.value() + " has no operation permits");
        }
    }

    private void canPlaceTransactionOnAccount(AccountId accountId) {
        checkAccountIsOpen(accountId, TransactionNotAllowedException::new);
    }

    private void canUpdateTransaction(Transaction transaction) {
        checkAccountIsOpen(transaction.getAccountId(), IllegalStateException::new);
    }

    private <T extends RuntimeException> void checkAccountIsOpen(AccountId accountId, Function<String, T> error) {
        var account = accountService.getById(accountId);
        if (!account.is(OPEN)) {
            throw error.apply("The account with ID " + accountId.value() + " is " + account.getState());
        }
    }

    private Transaction getTransactionById(TransactionId transactionId) {
        return repository.findTransactionById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction ID " + transactionId.value() + " not found"));
    }
}
