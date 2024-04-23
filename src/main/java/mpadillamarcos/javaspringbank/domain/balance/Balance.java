package mpadillamarcos.javaspringbank.domain.balance;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.exception.InsufficientBalanceException;
import mpadillamarcos.javaspringbank.domain.money.Money;

import static mpadillamarcos.javaspringbank.domain.money.Currency.EUR;
import static mpadillamarcos.javaspringbank.domain.money.Money.zero;
import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Builder(toBuilder = true)
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

    public Balance withdraw(Money amount) {
        if (this.amount.isLessThan(amount)) {
            throw new InsufficientBalanceException(
                    "The amount to withdraw (" + amount.getAmount() + ") exceeds the current balance " + this.amount
            );
        }

        return toBuilder()
                .amount(this.amount.subtract(amount))
                .build();
    }

    public Balance deposit(Money amount) {
        return toBuilder()
                .amount(this.amount.add(amount))
                .build();
    }

}
