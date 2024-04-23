package mpadillamarcos.javaspringbank.domain.balance;

import mpadillamarcos.javaspringbank.domain.account.AccountId;

import java.util.List;
import java.util.Set;

public interface BalanceRepository {

    void insert(Balance balance);

    Balance getBalance(AccountId accountId);

    List<Balance> getBalances(Set<AccountId> accountIds);

    void update(Balance updatedBalance);
}
