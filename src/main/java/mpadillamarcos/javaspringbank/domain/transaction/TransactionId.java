package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import mpadillamarcos.javaspringbank.domain.Id;

import java.util.UUID;

import static java.util.UUID.randomUUID;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TransactionId extends Id<UUID> {

    public TransactionId(UUID value) {
        super(value);
    }

    public static TransactionId randomTransactionId() {
        return transactionId(randomUUID());
    }

    private static TransactionId transactionId(UUID value) {
        return new TransactionId(value);
    }
}
