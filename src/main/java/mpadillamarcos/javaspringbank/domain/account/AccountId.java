package mpadillamarcos.javaspringbank.domain.account;

import lombok.EqualsAndHashCode;
import mpadillamarcos.javaspringbank.domain.Id;

import java.util.UUID;

import static java.util.UUID.randomUUID;

@EqualsAndHashCode(callSuper = true)
public class AccountId extends Id<UUID> {

    public AccountId(UUID value) {
        super(value);
    }

    public static AccountId randomAccountId() {
        return accountId(randomUUID());
    }

    public static AccountId accountId(UUID value) {
        return new AccountId(value);
    }
}
