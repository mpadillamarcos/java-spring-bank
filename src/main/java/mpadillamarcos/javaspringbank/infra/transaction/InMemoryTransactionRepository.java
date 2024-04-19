package mpadillamarcos.javaspringbank.infra.transaction;

import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.transaction.Transaction;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionId;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionRepository;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionState;
import org.springframework.stereotype.Repository;

import java.util.*;

import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.transactionId;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<TransactionId, Transaction> transactions = new HashMap<>();
    private final Stack<UUID[]> accountIdTransactionIdPairs = new Stack<>();

    @Override
    public void insert(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        UUID[] pair = new UUID[]{transaction.getAccountId().value(), transaction.getId().value()};
        accountIdTransactionIdPairs.add(pair);
    }

    @Override
    public Optional<Transaction> findLastTransactionByAccountId(AccountId accountId) {
        TransactionId transactionId = null;
        for (UUID[] pair : accountIdTransactionIdPairs) {
            if (pair[0].equals(accountId.value())) {
                transactionId = transactionId(pair[1]);
                break;
            }
        }
        return findTransactionById(transactionId);
    }

    @Override
    public void updateState(TransactionId transactionId, TransactionState state) {
        var groupId = transactions.get(transactionId).getGroupId();

        int counter = 0;
        for (Transaction transaction : transactions.values()) {
            if (counter == 2) {
                break;
            }
            if (transaction.getGroupId().equals(groupId)) {
                var newTransaction = new Transaction(
                        transaction.getId(),
                        transaction.getGroupId(),
                        transaction.getUserId(),
                        transaction.getAccountId(),
                        transaction.getAmount(),
                        transaction.getCreatedDate(),
                        state,
                        transaction.getDirection(),
                        transaction.getType(),
                        transaction.getConcept()
                );
                transactions.replace(newTransaction.getId(), newTransaction);
                counter++;
            }
        }
    }

    public Optional<Transaction> findTransactionById(TransactionId transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }
}
