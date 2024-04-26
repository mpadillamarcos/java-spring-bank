package mpadillamarcos.javaspringbank.infra.account;

import mpadillamarcos.javaspringbank.domain.account.Account;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.account.AccountRepository;

import java.util.*;

public class InMemoryAccountRepository implements AccountRepository {

    private final Map<AccountId, Account> accounts = new HashMap<>();

    @Override
    public void insert(Account account) {
        if (accounts.containsKey(account.getId())) {
            throw new IllegalStateException("account already exists");
        }
        accounts.put(account.getId(), account);
    }

    @Override
    public void update(Account account) {
        if (!accounts.containsKey(account.getId())) {
            throw new IllegalStateException("account does not exist");
        }
        accounts.put(account.getId(), account);
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    @Override
    public List<Account> getAccounts(Set<AccountId> accountIds) {
        return accountIds.stream()
                .map(accounts::get)
                .toList();
    }
}
