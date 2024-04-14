package mpadillamarcos.javaspringbank.domain.access;

import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface AccountAccessRepository {

    void insert(AccountAccess accountAccess);

    Optional<AccountAccess> findGrantedAccountAccess(AccountId accountId, UserId userId);

    Optional<AccountAccess> findAccountAccess(AccountId accountId, UserId userId);

    List<AccountAccess> listGrantedAccountAccesses(UserId userId);

    void update(AccountAccess accountAccess);
}
