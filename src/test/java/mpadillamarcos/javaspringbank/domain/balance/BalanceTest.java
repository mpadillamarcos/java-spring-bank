package mpadillamarcos.javaspringbank.domain.balance;

import mpadillamarcos.javaspringbank.domain.exception.InsufficientBalanceException;
import mpadillamarcos.javaspringbank.domain.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyBalance;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.balance.Balance.newBalance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BalanceTest {

    @MethodSource("balancesWithMissingData")
    @ParameterizedTest(name = "{0}")
    void requires_mandatory_fields(String field, Balance.BalanceBuilder builder) {
        var exception = assertThrows(IllegalArgumentException.class, builder::build);

        assertThat(exception).hasMessage(field + " must not be null");
    }

    @Test
    void creates_balance_with_builder_values() {
        var accountId = randomAccountId();
        var balanceAmount = Money.eur(45.6);

        var balance = newBalance()
                .accountId(accountId)
                .amount(balanceAmount)
                .build();

        assertThat(balance)
                .returns(accountId, Balance::getAccountId)
                .returns(balanceAmount, Balance::getAmount);
    }

    @Test
    void throws_exception_when_current_balance_is_less_than_amount_to_withdraw() {
        var balance = dummyBalance().amount(Money.eur(50)).build();
        var amountToWithdraw = Money.eur(100);

        assertThrows(InsufficientBalanceException.class, () -> balance.withdraw(amountToWithdraw));
    }

    @Test
    void creates_new_balance_after_withdrawing() {
        var accountId = randomAccountId();
        var balanceAmount = Money.eur(45.6);
        var newBalanceAmount = Money.eur(35.6);

        var balance = dummyBalance()
                .accountId(accountId)
                .amount(balanceAmount)
                .build();

        assertThat(balance.withdraw(Money.eur(10)))
                .returns(newBalanceAmount, Balance::getAmount);
    }

    @Test
    void creates_new_balance_after_depositing() {
        var accountId = randomAccountId();
        var balanceAmount = Money.eur(150);
        var newBalanceAmount = Money.eur(200);

        var balance = newBalance()
                .accountId(accountId)
                .amount(balanceAmount)
                .build();

        assertThat(balance.deposit(Money.eur(50)))
                .returns(newBalanceAmount, Balance::getAmount);
    }

    static List<Arguments> balancesWithMissingData() {
        return List.of(
                Arguments.arguments("accountId", dummyBalance().accountId(null)),
                Arguments.arguments("amount", dummyBalance().amount(null))
        );
    }
}