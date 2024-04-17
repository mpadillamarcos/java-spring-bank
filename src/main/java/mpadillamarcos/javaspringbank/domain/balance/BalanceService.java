package mpadillamarcos.javaspringbank.domain.balance;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static mpadillamarcos.javaspringbank.domain.balance.Balance.newBalance;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository repository;

    public Balance createBalance(AccountId accountId) {
        var balance = newBalance().accountId(accountId).build();
        repository.insert(balance);
        return balance;
    }

    public Balance getBalance(AccountId accountId) {
        return null;
    }

    public List<Balance> getBalances(Set<AccountId> accountIds) {
        return null;
    }

    public Balance deposit(AccountId accountId, Money amount) {
        return null;
    }

    public Balance withdraw(AccountId accountId, Money amount) {
        return null;
    }

}
