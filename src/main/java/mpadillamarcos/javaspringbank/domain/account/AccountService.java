package mpadillamarcos.javaspringbank.domain.account;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.time.Clock;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountAccessService accessService;
    private final AccountRepository repository;
    private final Clock clock;

    public Account openAccount(UserId userId) {
        var account = newAccount()
                .userId(userId)
                .createdDate(clock.now())
                .build();

        repository.insert(account);
        accessService.grantAccess(account.getId(), userId, OWNER);

        return account;
    }

    public List<Account> listUserAccounts(UserId userId) {
        return repository.listUserAccounts(userId);
    }

    public Optional<Account> findUserAccount(UserId userId, AccountId accountId) {
        return repository.findUserAccount(userId, accountId);
    }

    public void blockUserAccount(UserId userId, AccountId accountId) {
        var account = getUserAccount(userId, accountId);

        repository.update(account.block());
    }

    public void reopenUserAccount(UserId userId, AccountId accountId) {
        var account = getUserAccount(userId, accountId);

        repository.update(account.reopen());
    }

    public void closeUserAccount(UserId userId, AccountId accountId) {
        var account = getUserAccount(userId, accountId);

        repository.update(account.close());
    }

    private Account getUserAccount(UserId userId, AccountId accountId) {
        return findUserAccount(userId, accountId)
                .orElseThrow(() -> new NotFoundException("account not found"));
    }

}
