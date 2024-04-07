package mpadillamarcos.javaspringbank.domain.account;

import mpadillamarcos.javaspringbank.infra.TestClock;
import mpadillamarcos.javaspringbank.infra.account.InMemoryAccountRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static mpadillamarcos.javaspringbank.domain.account.AccountState.OPEN;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static mpadillamarcos.javaspringbank.infra.TestClock.NOW;
import static org.assertj.core.api.Assertions.assertThat;

class AccountServiceTest {

    private final AccountRepository repository = new InMemoryAccountRepository();
    private final AccountService service = new AccountService(repository, new TestClock());

    @Nested
    class OpenAccount {

        @Test
        void returns_a_new_account() {
            var userId = randomUserId();

            var account = service.openAccount(userId);

            assertThat(account)
                    .returns(OPEN, Account::getState)
                    .returns(userId, Account::getUserId)
                    .returns(NOW, Account::getCreatedDate);
        }

        @Test
        void persists_new_account() {
            var userId = randomUserId();

            var account = service.openAccount(userId);

            assertThat(repository.findUserAccount(userId, account.getId()))
                    .hasValue(account);
        }
    }

    @Nested
    class ListUserAccounts {
        @Test
        void returns_all_user_accounts() {
            var userId = randomUserId();
            var account1 = service.openAccount(userId);
            var account2 = service.openAccount(userId);

            var accounts = service.listUserAccounts(userId);

            assertThat(accounts).containsExactly(account1, account2);
        }
    }
}