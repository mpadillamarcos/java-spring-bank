package mpadillamarcos.javaspringbank.web.account;

import lombok.Builder;
import lombok.Data;
import mpadillamarcos.javaspringbank.domain.access.AccessType;
import mpadillamarcos.javaspringbank.domain.account.AccountState;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AccountViewDto {

    private UUID id;
    private UUID userId;
    private Instant createdDate;
    private AccountState state;
    private AccessType accessType;
}
