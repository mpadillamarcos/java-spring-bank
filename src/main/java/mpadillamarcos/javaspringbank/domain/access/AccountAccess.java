package mpadillamarcos.javaspringbank.domain.access;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.time.Instant;

import static mpadillamarcos.javaspringbank.domain.access.AccessState.GRANTED;
import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Builder(toBuilder = true)
@Value
public class AccountAccess {

    AccountId accountId;
    UserId userId;
    Instant createdDate;
    AccessType type;
    AccessState state;

    public AccountAccess(AccountId accountId, UserId userId, Instant createdDate, AccessType type, AccessState state) {
        this.accountId = require("accountId", accountId);
        this.userId = require("userId", userId);
        this.createdDate = require("createdDate", createdDate);
        this.type = require("type", type);
        this.state = require("state", state);
    }

    public static AccountAccessBuilder newAccountAccess() {
        return builder()
                .state(GRANTED);
    }
}
