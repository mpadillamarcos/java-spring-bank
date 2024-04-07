package mpadillamarcos.javaspringbank.domain.account;

import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.util.Optional;

public interface AccountRepository {

    void insert(Account account);

    Optional<Account> findUserAccount(UserId userId, AccountId accountId);
}
