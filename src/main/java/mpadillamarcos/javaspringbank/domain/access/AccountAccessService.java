package mpadillamarcos.javaspringbank.domain.access;

import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;

@Service
public class AccountAccessService {

    public void grantAccess(AccountId accountId, UserId userId, AccessType type) {

    }

    public void revokeAccess(AccountId accountId, UserId userId) {

    }
}
