package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.user.UserId;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class TransferRequest extends OperationRequest {

    private final AccountId destinationAccountId;
    private final TransactionType type;

    @Builder(builderMethodName = "transferRequestBuilder")
    public TransferRequest(AccountId originAccountId, UserId userId, Money amount, String concept, AccountId destinationAccountId, TransactionType type) {
        super(originAccountId, userId, amount, concept);
        this.destinationAccountId = destinationAccountId;
        this.type = type;
    }
}
