package mpadillamarcos.javaspringbank.domain.balance;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BalanceService {

    public Balance getBalance(AccountId accountId) {
        return null;
    }

    public List<Balance> getBalances(Set<AccountId> accountIds) {
        return null;
    }

    public Balance createBalance(AccountId accountId) {
        return null;
    }

    public Balance deposit(AccountId accountId, Money amount) {
        return null;
    }

    public Balance withdraw(AccountId accountId, Money amount) {
        return null;
    }

}
