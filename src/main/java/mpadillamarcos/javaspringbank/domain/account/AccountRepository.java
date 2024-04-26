package mpadillamarcos.javaspringbank.domain.account;

import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountRepository {

    void insert(Account account);

    Optional<Account> findUserAccount(UserId userId, AccountId accountId);

    void update(Account account);

    Optional<Account> findById(AccountId accountId);

    List<Account> getAccounts(Set<AccountId> accountIds);
}
