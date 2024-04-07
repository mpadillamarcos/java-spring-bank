package mpadillamarcos.javaspringbank.domain.account;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.time.Instant;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.BLOCKED;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.OPEN;
import static mpadillamarcos.javaspringbank.utils.Checks.require;
import static mpadillamarcos.javaspringbank.utils.Checks.requireState;

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
                .state(OPEN);
    }

    public Account block() {
        if (state == BLOCKED) {
            return this;
        }
        requireState(state, OPEN);

        return toBuilder()
                .state(BLOCKED)
                .build();
    }

    public Account reopen() {
        if (state == OPEN) {
            return this;
        }
        requireState(state, BLOCKED);

        return toBuilder()
                .state(OPEN)
                .build();
    }
}
