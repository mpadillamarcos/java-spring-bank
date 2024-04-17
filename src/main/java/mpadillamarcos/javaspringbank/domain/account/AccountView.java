package mpadillamarcos.javaspringbank.domain.account;

import lombok.Value;
import mpadillamarcos.javaspringbank.domain.access.AccessType;
import mpadillamarcos.javaspringbank.domain.access.AccountAccess;
import mpadillamarcos.javaspringbank.domain.balance.Balance;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.time.Instant;

import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Value
public class AccountView {

    AccountId accountId;
    UserId userId;
    Instant createdDate;
    AccountState state;
    AccessType accessType;
    Money balance;

    public AccountView(Account account, AccountAccess access, Balance balance) {
        require("account", account);
        require("access", access);
        require("balance", balance);
        this.accountId = account.getId();
        this.userId = access.getUserId();
        this.createdDate = account.getCreatedDate();
        this.state = account.getState();
        this.accessType = access.getType();
        this.balance = balance.getAmount();
    }
}
