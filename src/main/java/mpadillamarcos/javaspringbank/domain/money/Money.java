package mpadillamarcos.javaspringbank.domain.money;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static mpadillamarcos.javaspringbank.domain.money.Currency.EUR;
import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Value
@Builder(toBuilder = true)
public class Money {

    BigDecimal amount;
    Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        this.amount = require("amount", amount);
        this.currency = require("currency", currency);
    }

    public static Money eur(double amount) {
        return new Money(BigDecimal.valueOf(amount), EUR);
    }

    public static Money zero(Currency currency) {
        return new Money(ZERO, currency);
    }

    public Money subtract(Money amount) {
        checkCurrency(amount);

        return toBuilder()
                .amount(this.amount.subtract(amount.getAmount()))
                .build();
    }

    public Money add(Money amount) {
        checkCurrency(amount);

        return toBuilder()
                .amount(this.amount.add(amount.getAmount()))
                .build();
    }

    public Boolean isLessThan(Money amount) {
        return this.amount.compareTo(amount.getAmount()) == -1;
    }

    private void checkCurrency(Money amount) {
        if (!this.currency.equals(amount.currency)) {
            throw new IllegalArgumentException("The currencies are different");
        }
    }
}
