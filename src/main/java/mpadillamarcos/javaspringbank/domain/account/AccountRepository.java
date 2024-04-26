package mpadillamarcos.javaspringbank.domain.account;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountRepository {

    void insert(Account account);

    void update(Account account);

    Optional<Account> findById(AccountId accountId);

    List<Account> getAccounts(Set<AccountId> accountIds);
}
