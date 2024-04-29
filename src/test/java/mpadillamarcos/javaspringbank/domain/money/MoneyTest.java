package mpadillamarcos.javaspringbank.domain.money;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static mpadillamarcos.javaspringbank.domain.money.Currency.EUR;
import static mpadillamarcos.javaspringbank.domain.money.Currency.JPY;
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

    @Nested
    class Subtract {

        @Test
        void throws_exception_when_subtracting_different_currencies() {
            Money amount = Money.eur(50);
            Money withdrawal = Money.builder().currency(JPY).amount(BigDecimal.valueOf(1000)).build();

            assertThrows(IllegalArgumentException.class, () -> amount.subtract(withdrawal));
        }

        @Test
        void subtracts_amounts_with_same_currencies() {
            Money money = Money.eur(100);

            assertThat(money.subtract(Money.eur(20))).isEqualTo(Money.eur(80));
        }

    }

    @Nested
    class Add {

        @Test
        void throws_exception_when_adding_different_currencies() {
            Money amount = Money.eur(50);
            Money deposit = Money.builder().currency(JPY).amount(BigDecimal.valueOf(1000)).build();

            assertThrows(IllegalArgumentException.class, () -> amount.add(deposit));
        }

        @Test
        void adds_amounts_with_same_currencies() {
            Money money = Money.eur(100);

            assertThat(money.add(Money.eur(20))).isEqualTo(Money.eur(120));
        }
    }

    @Nested
    class IsLessThan {

        @Test
        void returns_true_when_current_amount_is_less_than_given_amount() {
            Money currentAmount = Money.eur(50);
            Money givenAmount = Money.eur(100);

            assertThat(currentAmount.isLessThan(givenAmount)).isEqualTo(true);
        }

        @Test
        void returns_false_when_current_amount_is_more_than_given_amount() {
            Money currentAmount = Money.eur(500);
            Money givenAmount = Money.eur(100);

            assertThat(currentAmount.isLessThan(givenAmount)).isEqualTo(false);
        }
    }

    @Test
    void returns_a_string_with_the_rounded_amount_and_the_currency_symbol() {
        var money = Money.eur(160.3654);

        assertThat(money.toString()).isEqualTo("€160.37");
    }

    @Test
    void returns_a_string_with_the_amount_with_no_decimals_and_the_currency_symbol() {
        var money = Money.builder().amount(BigDecimal.valueOf(100000.00)).currency(JPY).build();

        assertThat(money.toString()).isEqualTo("¥100000");
    }
}