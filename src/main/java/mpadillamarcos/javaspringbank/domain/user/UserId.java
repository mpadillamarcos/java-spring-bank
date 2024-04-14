package mpadillamarcos.javaspringbank.domain.user;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import mpadillamarcos.javaspringbank.domain.Id;

import java.util.UUID;

import static java.util.UUID.randomUUID;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UserId extends Id<UUID> {

    public UserId(UUID value) {
        super(value);
    }

    public UUID getValue() {
        return value;
    }

    public static UserId randomUserId() {
        return userId(randomUUID());
    }

    public static UserId userId(UUID value) {
        return new UserId(value);
    }
}
