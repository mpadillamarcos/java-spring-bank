package mpadillamarcos.javaspringbank.domain.account;

import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    void insert(Account account);

    Optional<Account> findUserAccount(UserId userId, AccountId accountId);

    List<Account> listUserAccounts(UserId userId);

    void update(Account account);
}
