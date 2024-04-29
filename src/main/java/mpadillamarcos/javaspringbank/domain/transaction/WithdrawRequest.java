package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Value
@Builder
public class WithdrawRequest {

    AccountId accountId;
    UserId userId;
    Money amount;
    String concept;

    public WithdrawRequest(AccountId accountId, UserId userId, Money amount, String concept) {
        this.accountId = require("accountId", accountId);
        this.userId = require("userId", userId);
        this.amount = require("amount", amount);
        this.concept = concept;
    }

    public static WithdrawRequestBuilder withdrawRequest() {
        return new WithdrawRequestBuilder();
    }
}
