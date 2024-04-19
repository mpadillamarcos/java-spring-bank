package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.user.UserId;

@Value
@Builder
public class TransactionRequest {

    AccountId originAccountId;
    AccountId destinationAccountId;
    UserId userId;
    Money amount;
    TransactionType type;
    String concept;
}
