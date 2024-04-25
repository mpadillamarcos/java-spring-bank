package mpadillamarcos.javaspringbank.infra.balance;

import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.infra.MapperTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyBalance;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {BalanceMapper.class})
public class BalanceMapperTest extends MapperTestBase {

    @Autowired
    private BalanceMapper mapper;

    @Test
    void returns_nothing_when_there_is_no_balance() {
        var balance = mapper.findBalance(randomAccountId());

        assertThat(balance).isEmpty();
    }

    @Test
    void returns_inserted_balance() {
        var balance = dummyBalance().build();
        mapper.insert(balance);

        var storedBalance = mapper.findBalance(balance.getAccountId());

        assertThat(storedBalance).hasValue(balance);
    }

    @Test
    void updates_balance() {
        var balance = dummyBalance().build();
        mapper.insert(balance);
        var updatedBalance = balance.deposit(Money.eur(100));
        mapper.update(updatedBalance);

        var storedBalance = mapper.findBalance(balance.getAccountId());

        assertThat(storedBalance).hasValue(updatedBalance);
    }

    @Test
    void returns_a_list_of_balances_given_a_set_of_account_ids() {
        var balance1 = dummyBalance().build();
        var balance2 = dummyBalance().build();
        var balance3 = dummyBalance().build();
        var balance4 = dummyBalance().build();
        var accountId1 = balance1.getAccountId();
        var accountId2 = balance2.getAccountId();
        var accountId4 = balance4.getAccountId();
        mapper.insert(balance1);
        mapper.insert(balance2);
        mapper.insert(balance3);
        mapper.insert(balance4);

        var storedBalance = mapper.findBalances(Set.of(accountId1, accountId2, accountId4));

        assertThat(storedBalance).containsExactly(balance1, balance2, balance4);
    }
}
