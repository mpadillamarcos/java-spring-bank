package mpadillamarcos.javaspringbank.domain.account;

import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.balance.BalanceService;
import mpadillamarcos.javaspringbank.infra.MapperTestBase;
import mpadillamarcos.javaspringbank.infra.TestClock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(
        classes = {
                AccountService.class,
                AccountAccessService.class,
                BalanceService.class,
                TestClock.class
        }
)
public class AccountServiceIT extends MapperTestBase {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;
    @SpyBean
    private BalanceService balanceService;

    @Nested
    class OpenAccount {

        @Test
        void rolls_changes_back_when_balance_cannot_be_created() {
            var userId = randomUserId();
            var captor = ArgumentCaptor.forClass(AccountId.class);
            doThrow(RuntimeException.class).when(balanceService).createBalance(captor.capture());

            assertThrows(RuntimeException.class, () -> accountService.openAccount(userId));

            assertThat(accountRepository.findById(captor.getValue())).isEmpty();
        }

        @Test
        void commits_transaction() {
            var userId = randomUserId();

            var account = accountService.openAccount(userId);

            assertThat(accountService.findAccountView(userId, account.getAccountId()))
                    .hasValue(account);
        }
    }
}
