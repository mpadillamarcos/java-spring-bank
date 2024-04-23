package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.account.AccountService;
import mpadillamarcos.javaspringbank.domain.balance.BalanceService;
import mpadillamarcos.javaspringbank.domain.exception.AccessDeniedException;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.exception.TransactionNotAllowedException;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.time.Clock;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.List;

import static mpadillamarcos.javaspringbank.domain.account.AccountState.OPEN;
import static mpadillamarcos.javaspringbank.domain.transaction.Transaction.newTransaction;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.INCOMING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.OUTGOING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.CONFIRMED;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.PENDING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountService accountService;
    private final AccountAccessService accessService;
    private final BalanceService balanceService;
    private final Clock clock;

    public void createTransfer(TransferRequest transferRequest) {
        var userId = transferRequest.getUserId();
        var originAccountId = transferRequest.getAccountId();
        var destinationAccountId = transferRequest.getDestinationAccountId();
        var groupId = randomTransactionGroupId();

        checkOriginAccount(originAccountId, userId);
        checkDestinationAccount(destinationAccountId);
        updateOriginAccountBalance(originAccountId, transferRequest.getAmount());

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

        checkOriginAccount(accountId, userId);
        updateOriginAccountBalance(accountId, withdrawRequest.getAmount());

        var withdrawTransaction = newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(userId)
                .accountId(accountId)
                .amount(withdrawRequest.getAmount())
                .createdDate(clock.now())
                .type(WITHDRAW)
                .state(CONFIRMED)
                .direction(OUTGOING)
                .concept(withdrawRequest.getConcept())
                .build();

        repository.insert(withdrawTransaction);
    }

    public void deposit(DepositRequest depositRequest) {
        var userId = depositRequest.getUserId();
        var accountId = depositRequest.getAccountId();

        checkOriginAccount(accountId, userId);
        balanceService.deposit(accountId, depositRequest.getAmount());

        var depositTransaction = newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(userId)
                .accountId(accountId)
                .amount(depositRequest.getAmount())
                .createdDate(clock.now())
                .type(DEPOSIT)
                .state(CONFIRMED)
                .direction(INCOMING)
                .concept(depositRequest.getConcept())
                .build();

        repository.insert(depositTransaction);
    }

    public void confirmTransaction(TransactionId transactionId) {
        var transaction = repository.findTransactionById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction ID " + transactionId + " not found"));

        List<Transaction> transactions = repository.findTransactionsByGroupId(transaction.getGroupId());

        checkTransactions(transactions);

        operateTransactions(transactions);
    }

    private void operateTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            if (transaction.getDirection().equals(INCOMING)) {
                balanceService.deposit(transaction.getAccountId(), transaction.getAmount());
            }
            if (transaction.getDirection().equals(OUTGOING) && !transaction.getType().equals(TRANSFER)) {
                balanceService.withdraw(transaction.getAccountId(), transaction.getAmount());
            }
            repository.update(transaction.confirm());
        }
    }

    private void checkTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            if (transaction.getState() != PENDING) {
                throw new IllegalStateException("Transaction with ID " + transaction.getId() + " is " + transaction.getState());
            }
            var accountId = transaction.getAccountId();
            var account = accountService.findById(accountId)
                    .orElseThrow(() -> new NotFoundException("Account with ID " + accountId + " was not found"));
            if (account.getState() != OPEN) {
                throw new IllegalStateException("Account with ID " + accountId + " is not open");
            }

        }
    }

    private void checkOriginAccount(AccountId originAccountId, UserId userId) {
        var access = accessService.findAccountAccess(originAccountId, userId)
                .orElseThrow(() -> new AccessDeniedException("User " + userId.value() + " has no access to that account"));
        if (!access.canOperate()) {
            throw new AccessDeniedException("User " + userId.value() + " has no access to that account");
        }
        var account = accountService.getById(originAccountId);
        if (account.getState() != OPEN) {
            throw new TransactionNotAllowedException("Origin account is " + account.getState());
        }
    }

    private void checkDestinationAccount(AccountId accountId) {
        var account = accountService.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account " + accountId.value() + " does not exist"));

        if (account.getState() != OPEN) {
            throw new TransactionNotAllowedException("The destination account is " + account.getState());
        }
    }

    private void updateOriginAccountBalance(AccountId originAccountId, Money amount) {
        balanceService.withdraw(originAccountId, amount);
    }

}
