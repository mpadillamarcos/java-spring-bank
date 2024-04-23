package mpadillamarcos.javaspringbank.infra.balance;

import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.balance.Balance;
import mpadillamarcos.javaspringbank.domain.balance.BalanceRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

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

    @Override
    public List<Balance> getBalances(Set<AccountId> accountIds) {
        List<Balance> balancesList = new ArrayList<>();
        for (AccountId accountId : accountIds) {
            balancesList.add(balances.get(accountId));
        }
        return balancesList;
    }

    @Override
    public void update(Balance updatedBalance) {
        balances.put(updatedBalance.getAccountId(), updatedBalance);
    }
}
