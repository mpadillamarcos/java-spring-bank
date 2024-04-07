package mpadillamarcos.javaspringbank.domain.account;

import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import mpadillamarcos.javaspringbank.infra.TestClock;
import mpadillamarcos.javaspringbank.infra.account.InMemoryAccountRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccount;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.BLOCKED;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.OPEN;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static mpadillamarcos.javaspringbank.infra.TestClock.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            var account1 = dummyAccount()
                    .createdDate(now())
                    .userId(userId)
                    .build();
            var account2 = dummyAccount()
                    .createdDate(now().plus(1, DAYS))
                    .userId(userId)
                    .build();

            repository.insert(account1);
            repository.insert(account2);

            var accounts = service.listUserAccounts(userId);

            assertThat(accounts).containsExactly(account1, account2);
        }
    }

    @Nested
    class FindUserAccount {

        @Test
        void returns_one_user_account() {
            var userId = randomUserId();
            var account = service.openAccount(userId);

            var response = service.findUserAccount(userId, account.getId());

            assertThat(response).contains(account);
        }
    }

    @Nested
    class BlockUserAccount {

        @Test
        void throws_not_found_exception_when_account_does_not_exist() {
            var userId = randomUserId();
            var accountId = randomAccountId();

            assertThrows(NotFoundException.class, () -> service.blockUserAccount(userId, accountId));
        }

        @Test
        void blocks_user_account() {
            var userId = randomUserId();
            var account = service.openAccount(userId);

            service.blockUserAccount(userId, account.getId());

            assertStateIs(userId, account, BLOCKED);
        }
    }

    @Nested
    class ReopenUserAccount {

        @Test
        void throws_not_found_exception_when_account_does_not_exist() {
            var userId = randomUserId();
            var accountId = randomAccountId();

            assertThrows(NotFoundException.class, () -> service.reopenUserAccount(userId, accountId));
        }

        @Test
        void reopens_user_account() {
            var userId = randomUserId();
            var account = service.openAccount(userId);
            service.blockUserAccount(userId, account.getId());

            service.reopenUserAccount(userId, account.getId());

            assertStateIs(userId, account, OPEN);
        }
    }

    private void assertStateIs(UserId userId, Account account, AccountState state) {
        assertThat(repository.findUserAccount(userId, account.getId()))
                .get()
                .returns(state, Account::getState);
    }
}