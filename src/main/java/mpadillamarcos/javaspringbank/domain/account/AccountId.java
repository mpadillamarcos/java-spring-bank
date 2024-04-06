package mpadillamarcos.javaspringbank.domain.account;

import lombok.Value;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Value
public class AccountId {

    UUID id;

    public AccountId(UUID id) {
        this.id = require("id", id);
    }

    public static AccountId randomAccountId() {
        return accountId(randomUUID());
    }

    public static AccountId accountId(UUID id) {
        return new AccountId(id);
    }
}
