package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.*;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.user.UserId;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Getter
@Builder
public class OperationRequest {

    private final AccountId accountId;
    private final UserId userId;
    private final Money amount;
    private final String concept;

}
