package mpadillamarcos.javaspringbank.domain.balance;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;

import static mpadillamarcos.javaspringbank.domain.money.Currency.EUR;
import static mpadillamarcos.javaspringbank.domain.money.Money.zero;
import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Builder
@Value
public class Balance {

    AccountId accountId;
    Money amount;

    public Balance(AccountId accountId, Money amount) {
        this.accountId = require("accountId", accountId);
        this.amount = require("amount", amount);
    }

    public static BalanceBuilder newBalance() {
        return builder()
                .amount(zero(EUR));
    }
}
