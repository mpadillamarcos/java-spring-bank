package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.WITHDRAW;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class WithdrawRequest extends OperationRequest {

    private final TransactionType type;

    @Builder(builderMethodName = "withdrawRequestBuilder")
    public WithdrawRequest(AccountId accountId, UserId userId, Money amount, String concept, TransactionType type) {
        super(accountId, userId, amount, concept);
        this.type = WITHDRAW;
    }
}
