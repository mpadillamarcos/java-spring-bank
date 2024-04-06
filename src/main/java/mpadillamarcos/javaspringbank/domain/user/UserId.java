package mpadillamarcos.javaspringbank.domain.user;

import lombok.Value;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Value
public class UserId {

    UUID id;

    public UserId(UUID id) {
        this.id = require("id", id);
    }

    public static UserId randomUserId() {
        return userId(randomUUID());
    }

    public static UserId userId(UUID id) {
        return new UserId(id);
    }
}
