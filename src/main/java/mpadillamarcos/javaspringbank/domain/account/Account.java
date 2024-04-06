package mpadillamarcos.javaspringbank.domain.account;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.time.Instant;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.OPENED;
import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Builder(toBuilder = true)
@Value
public class Account {

    AccountId id;
    UserId userId;
    Instant createdDate;
    AccountState state;

    public Account(AccountId id, UserId userId, Instant createdDate, AccountState state) {
        this.id = require("id", id);
        this.userId = require("userId", userId);
        this.createdDate = require("createdDate", createdDate);
        this.state = require("state", state);
    }

    public static AccountBuilder newAccount() {
        return builder()
                .id(randomAccountId())
                .state(OPENED);
    }
}
