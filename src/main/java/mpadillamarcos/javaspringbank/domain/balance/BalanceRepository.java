package mpadillamarcos.javaspringbank.domain.balance;

import mpadillamarcos.javaspringbank.domain.account.AccountId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BalanceRepository {

    void insert(Balance balance);

    Optional<Balance> findBalance(AccountId accountId);

    List<Balance> findBalances(Set<AccountId> accountIds);

    void update(Balance updatedBalance);
}
