package mpadillamarcos.javaspringbank.domain.balance;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.exception.InsufficientBalanceException;
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
        return repository.getBalance(accountId);
    }

    public List<Balance> getBalances(Set<AccountId> accountIds) {
        return repository.getBalances(accountIds);
    }

    public Balance deposit(AccountId accountId, Money amount) {
        return null;
    }

    public Balance withdraw(AccountId accountId, Money withdrawal) {
        var oldAmount = getBalance(accountId).getAmount().getAmount();
        int amountsComparison = oldAmount.compareTo(withdrawal.getAmount());
        if (amountsComparison < 0) {
            throw new InsufficientBalanceException(
                    "The withdrawal (" + withdrawal.getAmount() + ") exceeds the current balance in the account"
            );
        }
        var newAmount = oldAmount.subtract(withdrawal.getAmount()).doubleValue();
        var newBalance = newBalance().accountId(accountId).amount(Money.eur(newAmount)).build();
        repository.insert(newBalance);
        return newBalance;
    }

}
