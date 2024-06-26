package mpadillamarcos.javaspringbank.domain.access;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.time.Instant;

import static mpadillamarcos.javaspringbank.domain.access.AccessState.GRANTED;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.REVOKED;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.VIEWER;
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

    public AccountAccess grant(AccessType type) {
        if (this.type == OWNER) {
            throw new IllegalArgumentException("Owner access cannot be changed to another access type");
        }
        if (type == OWNER) {
            throw new IllegalArgumentException("Access type cannot be upgraded to owner");
        }
        if (this.type == type && this.state == GRANTED) {
            return this;
        }
        return toBuilder()
                .state(GRANTED)
                .type(type)
                .build();
    }

    public AccountAccess revoke() {
        if (this.state == REVOKED) {
            return this;
        }
        return toBuilder()
                .state(REVOKED)
                .build();
    }

    public boolean canOperate() {
        return (this.type != VIEWER && this.state != REVOKED);
    }
}
