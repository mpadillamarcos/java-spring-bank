package mpadillamarcos.javaspringbank.web.access;

import lombok.Builder;
import lombok.Data;
import mpadillamarcos.javaspringbank.domain.access.AccessState;
import mpadillamarcos.javaspringbank.domain.access.AccessType;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AccountAccessDto {

    private UUID accountId;
    private UUID userId;
    private Instant createdDate;
    private AccessState state;
    private AccessType type;
}
