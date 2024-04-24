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

import static mpadillamarcos.javaspringbank.domain.access.AccessState.REVOKED;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.VIEWER;
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
        var groupId = randomTransactionGroupId();

        canOperate(originAccountId, userId);

        exists(destinationAccountId);

        balanceService.withdraw(originAccountId, transferRequest.getAmount());

        var outgoingTransaction = newTransaction()
                .groupId(groupId)
                .userId(userId)
                .accountId(originAccountId)
                .amount(transferRequest.getAmount())
                .createdDate(clock.now())
                .type(TRANSFER)
                .direction(OUTGOING)
                .concept(transferRequest.getConcept())
                .build();

        var incomingTransaction = newTransaction()
                .groupId(groupId)
                .userId(userId)
                .accountId(destinationAccountId)
                .amount(transferRequest.getAmount())
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

    public void confirm(TransactionId transactionId) {
        var transaction = repository.findTransactionById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction ID " + transactionId.value() + " not found"));

        List<Transaction> transactions = repository.findTransactionsByGroupId(transaction.getGroupId());

        transactions.forEach(this::isAccountOpen);

        transactions.forEach(this::confirm);
    }

    private void confirm(Transaction transaction) {
        repository.update(transaction.confirm());
        if (transaction.is(INCOMING)) {
            balanceService.deposit(transaction.getAccountId(), transaction.getAmount());
        }
        if (transaction.is(OUTGOING) && !transaction.is(TRANSFER)) {
            balanceService.withdraw(transaction.getAccountId(), transaction.getAmount());
        }
    }

    private void isAccountOpen(Transaction transaction) {
        var account = accountService.getById(transaction.getAccountId());
        account.isOpen();
    }

    private void canOperate(AccountId accountId, UserId userId) {
        exists(accountId);

        var access = accessService.findAccountAccess(accountId, userId)
                .orElseThrow(() -> new AccessDeniedException("User with ID " + userId.value() + " has no access to that account"));

        if (access.getState().equals(REVOKED) || access.getType().equals(VIEWER)) {
            throw new AccessDeniedException("User with ID " + userId.value() + " has no operation permits");
        }
    }

    private void exists(AccountId accountId) {
        var account = accountService.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account with ID " + accountId.value() + " does not exist"));

        if (account.getState() != OPEN) {
            throw new TransactionNotAllowedException("The account with ID " + accountId.value() + " is " + account.getState());
        }
    }
}
