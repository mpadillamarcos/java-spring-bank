package mpadillamarcos.javaspringbank.infra.balance;

import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.balance.Balance;
import mpadillamarcos.javaspringbank.domain.balance.BalanceRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryBalanceRepository implements BalanceRepository {

    private final Map<AccountId, Balance> balances = new HashMap<>();

    @Override
    public void insert(Balance balance) {
        balances.put(balance.getAccountId(), balance);
    }

    @Override
    public Balance getBalance(AccountId accountId) {
        return balances.get(accountId);
    }
}
