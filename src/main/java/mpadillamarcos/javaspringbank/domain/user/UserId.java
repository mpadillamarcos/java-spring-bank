package mpadillamarcos.javaspringbank.domain.user;

import mpadillamarcos.javaspringbank.domain.Id;

import java.util.UUID;

import static java.util.UUID.randomUUID;

public class UserId extends Id<UUID> {

    public UserId(UUID value) {
        super(value);
    }

    public static UserId randomUserId() {
        return userId(randomUUID());
    }

    public static UserId userId(UUID value) {
        return new UserId(value);
    }
}
