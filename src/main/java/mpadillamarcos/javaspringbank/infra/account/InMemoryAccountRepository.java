package mpadillamarcos.javaspringbank.infra.account;

import mpadillamarcos.javaspringbank.domain.account.Account;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.account.AccountRepository;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;

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

    @Override
    public List<Account> listUserAccounts(UserId userId) {
        return accounts.values().stream()
                .filter(account -> account.getUserId().equals(userId))
                .sorted(comparing(Account::getCreatedDate))
                .toList();
    }

    @Override
    public void update(Account account) {
        if (!accounts.containsKey(account.getId())) {
            throw new IllegalStateException("account does not exist");
        }
        accounts.put(account.getId(), account);
    }
}
