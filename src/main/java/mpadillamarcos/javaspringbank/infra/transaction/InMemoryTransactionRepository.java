package mpadillamarcos.javaspringbank.infra.transaction;

import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.transaction.Transaction;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionId;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.transactionId;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<TransactionId, Transaction> transactions = new HashMap<>();
    private final Map<TransactionGroupId, List<TransactionId>> groupIdTransactionId = new HashMap<>();
    private final Stack<UUID[]> accountIdTransactionIdPairs = new Stack<>();

    @Override
    public void insert(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        UUID[] pair = new UUID[]{transaction.getAccountId().value(), transaction.getId().value()};
        accountIdTransactionIdPairs.add(pair);

        var groupId = transaction.getGroupId();
        List<TransactionId> transactionIds = groupIdTransactionId.get(groupId);
        if (transactionIds == null) {
            List<TransactionId> newTransactionIds = new ArrayList<>();
            newTransactionIds.add(transaction.getId());
            groupIdTransactionId.put(groupId, newTransactionIds);
        } else {
            transactionIds.add(transaction.getId());
            groupIdTransactionId.put(groupId, transactionIds);
        }
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
    public void update(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
    }

    @Override
    public List<Transaction> findTransactionsByGroupId(TransactionGroupId groupId) {
        List<TransactionId> transactionIds = groupIdTransactionId.get(groupId);
        List<Transaction> transactionsByGroupId = new ArrayList<>();
        for (TransactionId transactionId : transactionIds) {
            transactionsByGroupId.add(transactions.get(transactionId));
        }
        return transactionsByGroupId;
    }

    public Optional<Transaction> findTransactionById(TransactionId transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }
}
