package mpadillamarcos.javaspringbank.domain.balance;

import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.infra.balance.InMemoryBalanceRepository;
import org.junit.jupiter.api.Test;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.money.Currency.EUR;
import static org.assertj.core.api.Assertions.assertThat;

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
}