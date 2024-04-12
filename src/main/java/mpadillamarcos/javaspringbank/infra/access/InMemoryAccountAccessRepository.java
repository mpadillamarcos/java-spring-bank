package mpadillamarcos.javaspringbank.infra.access;

import mpadillamarcos.javaspringbank.domain.access.AccountAccess;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessRepository;
import mpadillamarcos.javaspringbank.domain.account.Account;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;

@Repository
public class InMemoryAccountAccessRepository implements AccountAccessRepository {

    private final Map<AccountAccessKey, AccountAccess> accountAccesses = new HashMap<>();

    @Override
    public void insert(AccountAccess accountAccess) {
        var key = new AccountAccessKey(accountAccess.getAccountId(), accountAccess.getUserId());
        if (accountAccesses.containsKey(key)) {
            throw new IllegalStateException("account access already exists");
        }
        accountAccesses.put(key, accountAccess);
    }

    @Override
    public Optional<AccountAccess> findAccountAccess(AccountId accountId, UserId userId) {
        var key = new AccountAccessKey(accountId, userId);

        return Optional.ofNullable(accountAccesses.get(key))
                .filter(accountAccess -> accountAccess.getUserId().equals(userId))
                .filter(accountAccess -> accountAccess.getAccountId().equals(accountId));
    }

    @Override
    public List<AccountAccess> listAllAccountAccesses(UserId userId) {
        return accountAccesses.values().stream()
                .filter(access -> access.getUserId().equals(userId))
                .toList();
    }

    @Override
    public void update(AccountAccess accountAccess) {
        var key = new AccountAccessKey(accountAccess.getAccountId(), accountAccess.getUserId());
        if (!accountAccesses.containsKey(key)) {
            throw new IllegalStateException("account access does not exist");
        }
        accountAccesses.put(key, accountAccess);
    }

    private record AccountAccessKey(AccountId accountId, UserId userId) {
    }

}
