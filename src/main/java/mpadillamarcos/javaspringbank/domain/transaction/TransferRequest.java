package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Value
@Builder
public class TransferRequest {

    AccountId destinationAccountId;
    AccountId originAccountId;
    UserId userId;
    Money amount;
    String concept;

    public TransferRequest(AccountId destinationAccountId,
                           AccountId originAccountId,
                           UserId userId,
                           Money amount,
                           String concept) {
        this.originAccountId = require("originAccountId", originAccountId);
        this.destinationAccountId = require("destinationAccountId", destinationAccountId);
        this.userId = require("userId", userId);
        this.amount = require("amount", amount);
        this.concept = concept;
    }

    public static TransferRequestBuilder transferRequest() {
        return new TransferRequestBuilder();
    }
}
