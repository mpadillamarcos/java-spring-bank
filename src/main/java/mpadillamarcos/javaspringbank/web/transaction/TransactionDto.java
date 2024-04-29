package mpadillamarcos.javaspringbank.web.transaction;

import lombok.Builder;
import lombok.Data;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionState;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionType;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TransactionDto {

    private final UUID id;
    private final UUID userId;
    private final UUID accountId;
    private final Money amount;
    private final Instant createdDate;
    private final TransactionState state;
    private final TransactionDirection direction;
    private final TransactionType type;
    private final String concept;
}
