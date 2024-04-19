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

import static mpadillamarcos.javaspringbank.domain.account.AccountState.OPEN;
import static mpadillamarcos.javaspringbank.domain.transaction.Transaction.newTransaction;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.INCOMING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.OUTGOING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.CONFIRMED;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountService accountService;
    private final AccountAccessService accessService;
    private final BalanceService balanceService;
    private final Clock clock;

    public void createTransfer(TransactionRequest transactionRequest) {
        var userId = transactionRequest.getUserId();
        var originAccountId = transactionRequest.getOriginAccountId();
        var destinationAccountId = transactionRequest.getDestinationAccountId();
        var groupId = randomTransactionGroupId();

        checkOriginAccount(originAccountId, userId);
        checkDestinationAccount(destinationAccountId);
        updateOriginAccountBalance(originAccountId, transactionRequest.getAmount());

        var outgoingTransaction = newTransaction()
                .groupId(groupId)
                .userId(userId)
                .accountId(originAccountId)
                .amount(transactionRequest.getAmount())
                .createdDate(clock.now())
                .type(TRANSFER)
                .direction(OUTGOING)
                .concept(transactionRequest.getConcept())
                .build();

        var incomingTransaction = newTransaction()
                .groupId(groupId)
                .userId(userId)
                .accountId(destinationAccountId)
                .amount(transactionRequest.getAmount())
                .createdDate(clock.now())
                .type(TRANSFER)
                .direction(INCOMING)
                .concept(transactionRequest.getConcept())
                .build();

        repository.insert(outgoingTransaction);
        repository.insert(incomingTransaction);
    }

    public void withdraw(TransactionRequest transactionRequest) {
        var userId = transactionRequest.getUserId();
        var accountId = transactionRequest.getOriginAccountId();

        checkOriginAccount(accountId, userId);
        updateOriginAccountBalance(accountId, transactionRequest.getAmount());

        var withdrawTransaction = newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(userId)
                .accountId(accountId)
                .amount(transactionRequest.getAmount())
                .createdDate(clock.now())
                .type(WITHDRAW)
                .state(CONFIRMED)
                .direction(OUTGOING)
                .concept(transactionRequest.getConcept())
                .build();

        repository.insert(withdrawTransaction);
    }

    public void deposit(TransactionRequest transactionRequest) {
        var userId = transactionRequest.getUserId();
        var accountId = transactionRequest.getOriginAccountId();

        checkOriginAccount(accountId, userId);
        balanceService.deposit(accountId, transactionRequest.getAmount());

        var depositTransaction = newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(userId)
                .accountId(accountId)
                .amount(transactionRequest.getAmount())
                .createdDate(clock.now())
                .type(DEPOSIT)
                .state(CONFIRMED)
                .direction(INCOMING)
                .concept(transactionRequest.getConcept())
                .build();

        repository.insert(depositTransaction);
    }

    public void confirmTransaction(TransactionId transactionId) {
        var transaction = repository.findTransactionById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction ID " + transactionId + " not found"));

        balanceService.deposit(transaction.getAccountId(), transaction.getAmount());

        repository.updateState(transaction.getId(), CONFIRMED);
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

    private void checkDestinationAccount(AccountId destinationAccountId) {
        if (!accountService.exists(destinationAccountId)) {
            throw new NotFoundException("Account " + destinationAccountId.value() + " does not exist");
        }
        var account = accountService.getById(destinationAccountId);
        if (account.getState() != OPEN) {
            throw new TransactionNotAllowedException("The destination account is " + account.getState());
        }
    }

    private void updateOriginAccountBalance(AccountId originAccountId, Money amount) {
        balanceService.withdraw(originAccountId, amount);
    }

}