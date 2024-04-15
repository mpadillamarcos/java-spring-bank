package mpadillamarcos.javaspringbank.domain.money;

import lombok.Value;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static mpadillamarcos.javaspringbank.domain.money.Currency.EUR;

@Value
public class Money {

    BigDecimal amount;
    Currency currency;

    public static Money eur(double amount) {
        return new Money(BigDecimal.valueOf(amount), EUR);
    }

    public static Money zero(Currency currency) {
        return new Money(ZERO, currency);
    }
}
