package mpadillamarcos.javaspringbank.domain.account;

import lombok.Value;
import mpadillamarcos.javaspringbank.domain.access.AccessType;
import mpadillamarcos.javaspringbank.domain.access.AccountAccess;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.time.Instant;

import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Value
public class AccountView {

    AccountId accountId;
    UserId userId;
    Instant createdDate;
    AccountState state;
    AccessType type;

    public AccountView(Account account, AccountAccess access) {
        require("account", account);
        require("access", access);
        this.accountId = account.getId();
        this.userId = access.getUserId();
        this.createdDate = account.getCreatedDate();
        this.state = account.getState();
        this.type = access.getType();
    }
}
