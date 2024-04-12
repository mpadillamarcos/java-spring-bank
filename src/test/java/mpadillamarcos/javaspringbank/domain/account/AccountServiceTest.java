package mpadillamarcos.javaspringbank.domain.account;

import mpadillamarcos.javaspringbank.domain.access.AccountAccess;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import mpadillamarcos.javaspringbank.infra.TestClock;
import mpadillamarcos.javaspringbank.infra.account.InMemoryAccountRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccount;
import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccountAccess;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.VIEWER;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.*;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static mpadillamarcos.javaspringbank.infra.TestClock.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private final AccountRepository repository = new InMemoryAccountRepository();
    private final AccountAccessService accessService = mock(AccountAccessService.class);
    private final AccountService service = new AccountService(accessService, repository, new TestClock());

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

        @Test
        void grants_owner_access_to_user() {
            var userId = randomUserId();

            var account = service.openAccount(userId);

            verify(accessService, times(1))
                    .grantAccess(account.getId(), userId, OWNER);
        }
    }

    @Nested
    class ListUserAccounts {

        @Test
        void returns_all_user_account_views() {
            var userId = randomUserId();
            var account1 = dummyAccount()
                    .userId(userId)
                    .createdDate(now())
                    .build();
            var account2 = dummyAccount()
                    .createdDate(now().plus(1, DAYS))
                    .build();
            var access1 = dummyAccountAccess()
                    .accountId(account1.getId())
                    .userId(userId)
                    .build();
            var access2 = dummyAccountAccess()
                    .accountId(account2.getId())
                    .userId(userId)
                    .type(VIEWER)
                    .build();

            repository.insert(account1);
            repository.insert(account2);

            when(accessService.listAllAccountAccesses(userId))
                    .thenReturn(List.of(access1, access2));

            var accounts = service.listUserAccounts(userId);

            assertThat(accounts).hasSize(2);
            assertThat(accounts).containsExactly(
                    new AccountView(account1, access1),
                    new AccountView(account2, access2)
            );
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

    @Nested
    class CloseUserAccount {

        @Test
        void throws_not_found_exception_when_account_does_not_exist() {
            var userId = randomUserId();
            var accountId = randomAccountId();

            assertThrows(NotFoundException.class, () -> service.closeUserAccount(userId, accountId));
        }

        @Test
        void closes_user_account() {
            var userId = randomUserId();
            var account = service.openAccount(userId);

            service.closeUserAccount(userId, account.getId());

            assertStateIs(userId, account, CLOSED);
        }
    }

    private void assertStateIs(UserId userId, Account account, AccountState state) {
        assertThat(repository.findUserAccount(userId, account.getId()))
                .get()
                .returns(state, Account::getState);
    }
}