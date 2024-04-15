package mpadillamarcos.javaspringbank.domain.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static mpadillamarcos.javaspringbank.domain.money.Currency.EUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void requires_amount_field() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new Money(null, EUR));

        assertThat(exception).hasMessage("amount must not be null");
    }

    @Test
    void requires_currency_field() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new Money(BigDecimal.valueOf(100), null));

        assertThat(exception).hasMessage("currency must not be null");
    }

    @Test
    void creates_money_instance_with_provided_values() {
        BigDecimal amount = BigDecimal.valueOf(200);
        Currency currency = EUR;

        Money money = new Money(amount, currency);

        assertThat(money.getAmount()).isEqualTo(amount);
        assertThat(money.getCurrency()).isEqualTo(currency);
    }

    @Test
    void creates_money_with_euros_as_currency_value() {
        Money money = Money.eur(100);

        assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(money.getCurrency()).isEqualTo(EUR);
    }

    @Test
    void creates_money_with_zero_as_amount_value() {
        Money money = Money.zero(EUR);

        assertThat(money.getAmount()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(money.getCurrency()).isEqualTo(EUR);
    }
}