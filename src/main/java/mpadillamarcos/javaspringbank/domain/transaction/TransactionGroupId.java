package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import mpadillamarcos.javaspringbank.domain.Id;

import java.util.UUID;

import static java.util.UUID.randomUUID;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TransactionGroupId extends Id<UUID> {

    public TransactionGroupId(UUID value) {
        super(value);
    }

    public static TransactionGroupId randomTransactionGroupId() {
        return transactionGroupId(randomUUID());
    }

    private static TransactionGroupId transactionGroupId(UUID value) {
        return new TransactionGroupId(value);
    }
}
