package mpadillamarcos.javaspringbank.domain.balance;

import mpadillamarcos.javaspringbank.domain.account.AccountId;

public interface BalanceRepository {

    void insert(Balance balance);

    Balance getBalance(AccountId accountId);
}
