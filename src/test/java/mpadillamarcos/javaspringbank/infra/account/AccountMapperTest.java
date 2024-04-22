package mpadillamarcos.javaspringbank.infra.account;

import mpadillamarcos.javaspringbank.domain.account.Account;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.infra.MapperTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccount;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {AccountMapper.class})
class AccountMapperTest extends MapperTestBase {

    @Autowired
    private AccountMapper mapper;

    @Test
    void returns_empty_when_account_does_not_exist() {
        var account = mapper.findById(randomAccountId());

        assertThat(account).isEmpty();
    }

    @Test
    void returns_inserted_account() {
        var account = dummyAccount().build();
        mapper.insert(account);

        var storedAccount = mapper.findById(account.getId());

        assertThat(storedAccount).hasValue(account);
    }

    @Test
    void updates_an_already_inserted_account() {
        var account = dummyAccount().build();
        mapper.insert(account);

        mapper.update(account.block());

        var storedAccount = mapper.findById(account.getId());

        assertThat(storedAccount).hasValue(account.block());
    }

    @Test
    void returns_a_list_of_accounts() {
        var account1 = dummyAccount().build();
        var account2 = dummyAccount().build();
        mapper.insert(account1);
        mapper.insert(account2);
        mapper.insert(dummyAccount().build());
        Set<AccountId> accountIds = new HashSet<>();
        accountIds.add(account1.getId());
        accountIds.add(account2.getId());

        List<Account> accountsList = mapper.getAccounts(accountIds);

        assertThat(accountsList).containsExactly(account1, account2);
    }
}