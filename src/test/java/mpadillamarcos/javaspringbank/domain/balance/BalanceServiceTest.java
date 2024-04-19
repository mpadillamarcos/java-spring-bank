package mpadillamarcos.javaspringbank.domain.balance;

import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.exception.InsufficientBalanceException;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.infra.balance.InMemoryBalanceRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.balance.Balance.newBalance;
import static mpadillamarcos.javaspringbank.domain.money.Currency.EUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BalanceServiceTest {

    private final BalanceRepository repository = new InMemoryBalanceRepository();
    private final BalanceService service = new BalanceService(repository);

    @Test
    void creates_a_new_balance() {
        var accountId = randomAccountId();

        service.createBalance(accountId);

        assertThat(repository.getBalance(accountId))
                .returns(accountId, Balance::getAccountId)
                .returns(Money.zero(EUR), Balance::getAmount);
    }

    @Test
    void gets_one_balance_by_account_id() {
        var accountId = randomAccountId();

        service.createBalance(accountId);

        assertThat(service.getBalance(accountId))
                .returns(accountId, Balance::getAccountId)
                .returns(Money.zero(EUR), Balance::getAmount);
    }

    @Test
    void gets_multiple_balances_by_a_set_of_account_ids() {
        var accountId1 = randomAccountId();
        var accountId2 = randomAccountId();

        Set<AccountId> accountIds = new LinkedHashSet<>();
        accountIds.add(accountId1);
        accountIds.add(accountId2);

        var balance1 = service.createBalance(accountId1);
        var balance2 = service.createBalance(accountId2);

        assertThat(service.getBalances(accountIds))
                .containsExactlyElementsOf(List.of(balance1, balance2));
    }

    @Nested
    class Withdraw {

        @Test
        void throws_exception_when_withdrawal_exceeds_current_balance_amount() {
            var accountId = randomAccountId();
            service.createBalance(accountId);

            assertThrows(InsufficientBalanceException.class,
                    () -> service.withdraw(accountId, Money.eur(100))
            );
        }

        @Test
        void updates_balance_when_withdrawal_does_not_exceed_current_balance() {
            var accountId = randomAccountId();
            var balance = newBalance().accountId(accountId).amount(Money.eur(200)).build();

            repository.insert(balance);

            assertThat(service.withdraw(accountId, Money.eur(150)))
                    .returns(Money.eur(50), Balance::getAmount);
        }
    }

    @Nested
    class Deposit {

        @Test
        void updates_balance_with_deposit_amount() {
            var accountId = randomAccountId();
            var balance = newBalance().accountId(accountId).amount(Money.eur(200)).build();

            repository.insert(balance);

            assertThat(service.deposit(accountId, Money.eur(50)))
                    .returns(Money.eur(250), Balance::getAmount);
        }
    }


}