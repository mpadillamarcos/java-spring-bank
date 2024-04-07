package mpadillamarcos.javaspringbank.infra.account;

import mpadillamarcos.javaspringbank.domain.account.Account;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.account.AccountRepository;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
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
    public Optional<Account> findUserAccount(UserId userId, AccountId accountId) {
        return Optional.ofNullable(accounts.get(accountId))
                .filter(account -> account.getUserId().equals(userId));
    }
}
