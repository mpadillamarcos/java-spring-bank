package mpadillamarcos.javaspringbank.domain.access;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.time.Clock;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.newAccountAccess;

@Service
@RequiredArgsConstructor
public class AccountAccessService {

    private final AccountAccessRepository repository;
    private final Clock clock;

    public AccountAccess grantAccess(AccountId accountId, UserId userId, AccessType type) {
        var existingAccess = repository.findGrantedAccountAccess(accountId, userId);
        AccountAccess access;

        if (existingAccess.isPresent()) {
            access = existingAccess.get().grant(type);
            repository.update(access);
        } else {
            access = newAccountAccess()
                    .accountId(accountId)
                    .userId(userId)
                    .createdDate(clock.now())
                    .type(type)
                    .build();

            repository.insert(access);
        }
        return access;
    }

    public void revokeAccess(AccountId accountId, UserId userId) {
        var accountAccess = repository.findGrantedAccountAccess(accountId, userId)
                .orElseThrow(() -> new NotFoundException("account access not found"));

        repository.update(accountAccess.revoke());
    }

    public List<AccountAccess> listAllAccountAccesses(UserId userId) {
        return repository.listGrantedAccountAccesses(userId);
    }

    public Optional<AccountAccess> findAccountAccess(AccountId accountId, UserId userId) {
        return repository.findGrantedAccountAccess(accountId, userId);
    }
}
