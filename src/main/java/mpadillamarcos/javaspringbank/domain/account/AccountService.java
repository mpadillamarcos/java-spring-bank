package mpadillamarcos.javaspringbank.domain.account;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.time.Clock;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final Clock clock;

    public Account openAccount(UserId userId) {
        var account = newAccount()
                .userId(userId)
                .createdDate(clock.now())
                .build();

        repository.insert(account);

        return account;
    }

    public List<Account> listUserAccounts(UserId userId) {
        return repository.listUserAccounts(userId);
    }

    public Optional<Account> findUserAccount(UserId userId, AccountId accountId) {
        return repository.findUserAccount(userId, accountId);
    }

    public void blockUserAccount(UserId userid, AccountId accountId) {
        var account = findUserAccount(userid, accountId)
                .orElseThrow(() -> new NotFoundException("account not found"));

        repository.update(account.block());
    }

    public void reopenUserAccount(UserId userid, AccountId accountId) {

    }

    public void closeUserAccount(UserId userid, AccountId accountId) {

    }

}
