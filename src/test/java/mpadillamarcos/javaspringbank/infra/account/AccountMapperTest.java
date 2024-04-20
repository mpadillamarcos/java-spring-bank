package mpadillamarcos.javaspringbank.infra.account;

import mpadillamarcos.javaspringbank.infra.MapperTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccount;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.infra.TestClock.NOW;
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
        var account = dummyAccount().createdDate(NOW).build();
        mapper.insert(account);

        var storedAccount = mapper.findById(account.getId());

        assertThat(storedAccount).hasValue(account);
    }
}