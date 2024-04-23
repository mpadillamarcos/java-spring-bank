package mpadillamarcos.javaspringbank.domain.transaction;

import mpadillamarcos.javaspringbank.domain.account.AccountId;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    void insert(Transaction transaction);

    Optional<Transaction> findTransactionById(TransactionId transactionId);

    Optional<Transaction> findLastTransactionByAccountId(AccountId accountId);

    void update(Transaction transaction);

    List<Transaction> findTransactionsByGroupId(TransactionGroupId groupId);
}
