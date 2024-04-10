package mpadillamarcos.javaspringbank.domain.access;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.time.Clock;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;

import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.newAccountAccess;

@Service
@RequiredArgsConstructor
public class AccountAccessService {

    private final AccountAccessRepository repository;
    private final Clock clock;

    public void grantAccess(AccountId accountId, UserId userId, AccessType type) {
        var accountAccess = repository.findAccountAccess(accountId, userId);

        if (accountAccess.isPresent()) {
            repository.update(accountAccess.get().grant(type));
        } else {
            repository.insert(newAccountAccess()
                    .accountId(accountId)
                    .userId(userId)
                    .createdDate(clock.now())
                    .type(type)
                    .build());
        }
    }

    public void revokeAccess(AccountId accountId, UserId userId) {
        var accountAccess = repository.findAccountAccess(accountId, userId)
                .orElseThrow(() -> new NotFoundException("account access not found"));

        repository.update(accountAccess.revoke());
    }
}
