package mpadillamarcos.javaspringbank.infra.transaction;

import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.transaction.Transaction;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionId;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionRepository;
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

    public Optional<Transaction> findTransactionById(TransactionId transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }
}
