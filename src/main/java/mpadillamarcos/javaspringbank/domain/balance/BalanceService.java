package mpadillamarcos.javaspringbank.domain.balance;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
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
        return repository.findBalance(accountId)
                .orElseThrow(() -> new NotFoundException("balance not found"));
    }

    public List<Balance> getBalances(Set<AccountId> accountIds) {
        return repository.findBalances(accountIds);
    }

    public void withdraw(AccountId accountId, Money amount) {
        var currentBalance = getBalance(accountId);
        var updatedBalance = currentBalance.withdraw(amount);
        repository.update(updatedBalance);
    }

    public void deposit(AccountId accountId, Money amount) {
        var currentBalance = getBalance(accountId);
        var updatedBalance = currentBalance.deposit(amount);
        repository.update(updatedBalance);
    }

}
