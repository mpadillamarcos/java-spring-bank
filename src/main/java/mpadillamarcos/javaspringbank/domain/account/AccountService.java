package mpadillamarcos.javaspringbank.domain.account;

import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;

@Service
public class AccountService {

    public Account openAccount(UserId userId) {
        return null;
    }

    public List<Account> listUserAccounts(UserId userId) {
        return null;
    }

    public Optional<Account> findUserAccount(UserId userId, AccountId id) {
        return empty();
    }

    public void blockAccount(AccountId id) {

    }

    public void reactivate(AccountId id) {

    }

    public void closeAccount(AccountId id) {

    }

}
