package mpadillamarcos.javaspringbank.domain.account;

import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.balance.BalanceService;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import mpadillamarcos.javaspringbank.infra.TestClock;
import mpadillamarcos.javaspringbank.infra.account.InMemoryAccountRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static mpadillamarcos.javaspringbank.domain.Instances.*;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.VIEWER;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.*;
import static mpadillamarcos.javaspringbank.domain.money.Money.eur;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static mpadillamarcos.javaspringbank.infra.TestClock.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private final AccountRepository repository = new InMemoryAccountRepository();
    private final BalanceService balanceService = mock(BalanceService.class);
    private final AccountAccessService accessService = mock(AccountAccessService.class);
    private final AccountService service = new AccountService(
            balanceService,
            accessService,
            repository,
            new TestClock()
    );

    @Nested
    class OpenAccount {

        @Test
        void returns_an_account_view() {
            var userId = randomUserId();
            var access = dummyAccountAccess().userId(userId).build();
            var balance = dummyBalance().build();

            when(accessService.grantAccess(any(), any(), any())).thenReturn(access);
            when(balanceService.createBalance(any())).thenReturn(balance);

            var account = service.openAccount(userId);

            assertThat(account)
                    .returns(OPEN, AccountView::getState)
                    .returns(userId, AccountView::getUserId)
                    .returns(NOW, AccountView::getCreatedDate)
                    .returns(OWNER, AccountView::getAccessType)
                    .returns(balance.getAmount(), AccountView::getBalance);
        }

        @Test
        void persists_new_account() {
            var userId = randomUserId();
            var access = dummyAccountAccess().userId(userId).build();
            var balance = dummyBalance().build();

            when(accessService.grantAccess(any(), any(), any())).thenReturn(access);
            when(balanceService.createBalance(any())).thenReturn(balance);

            var account = service.openAccount(userId);

            assertThat(repository.findUserAccount(userId, account.getAccountId()))
                    .get()
                    .returns(account.getAccountId(), Account::getId)
                    .returns(userId, Account::getUserId)
                    .returns(NOW, Account::getCreatedDate)
                    .returns(OPEN, Account::getState);
        }

        @Test
        void grants_owner_access_to_user() {
            var userId = randomUserId();
            var access = dummyAccountAccess().userId(userId).build();
            var balance = dummyBalance().build();

            when(accessService.grantAccess(any(), any(), any())).thenReturn(access);
            when(balanceService.createBalance(any())).thenReturn(balance);

            var account = service.openAccount(userId);

            verify(accessService, times(1))
                    .grantAccess(account.getAccountId(), userId, OWNER);
        }

        @Test
        void creates_balance_when_opening_an_account() {
            var userId = randomUserId();
            var access = dummyAccountAccess().userId(userId).build();
            var balance = dummyBalance().build();

            when(accessService.grantAccess(any(), any(), any())).thenReturn(access);
            when(balanceService.createBalance(any())).thenReturn(balance);

            var account = service.openAccount(userId);

            verify(balanceService, times(1))
                    .createBalance(account.getAccountId());
        }
    }

    @Nested
    class ListUserAccounts {

        @Test
        void returns_all_user_account_views() {
            var userId = randomUserId();
            var account1 = createAccount(dummyAccount().userId(userId).createdDate(now()));
            var account2 = createAccount(dummyAccount().createdDate(now().plus(1, DAYS)));
            var access1 = dummyAccountAccess()
                    .accountId(account1.getId())
                    .userId(userId)
                    .build();
            var access2 = dummyAccountAccess()
                    .accountId(account2.getId())
                    .userId(userId)
                    .type(VIEWER)
                    .build();
            var balance1 = dummyBalance().accountId(account1.getId()).amount(eur(20)).build();
            var balance2 = dummyBalance().accountId(account2.getId()).amount(eur(50)).build();

            when(accessService.listAllAccountAccesses(userId))
                    .thenReturn(List.of(access1, access2));
            when(balanceService.getBalances(Set.of(account1.getId(), account2.getId())))
                    .thenReturn(List.of(balance1, balance2));

            var accounts = service.listUserAccounts(userId);

            assertThat(accounts).hasSize(2);
            assertThat(accounts).containsExactly(
                    new AccountView(account1, access1, balance1),
                    new AccountView(account2, access2, balance2)
            );
        }
    }

    @Nested
    class FindUserAccount {

        @Test
        void returns_one_user_account_view() {
            var userId = randomUserId();
            var accountId = randomAccountId();
            var account = createAccount(dummyAccount().id(accountId).userId(userId));
            var access = dummyAccountAccess().accountId(accountId).userId(userId).build();
            var balance = dummyBalance().accountId(accountId).build();
            var accountView = new AccountView(account, access, balance);

            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));
            when(balanceService.getBalance(accountId)).thenReturn(balance);

            var response = service.findUserAccount(userId, accountId);

            assertThat(response).isEqualTo(Optional.of(accountView));
        }

        @Test
        void returns_account_for_non_owner() {
            var userId = randomUserId();
            var accountId = randomAccountId();
            var account = createAccount(dummyAccount().id(accountId));
            var access = dummyAccountAccess()
                    .accountId(accountId)
                    .userId(userId)
                    .type(VIEWER)
                    .build();
            var balance = dummyBalance().accountId(accountId).build();
            var accountView = new AccountView(account, access, balance);

            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));
            when(balanceService.getBalance(accountId)).thenReturn(balance);

            var response = service.findUserAccount(userId, accountId);

            assertThat(response).isEqualTo(Optional.of(accountView));
        }
    }

    @Nested
    class IsAccountPresent {

        @Test
        void returns_false_when_account_does_not_exist() {
            var accountId = randomAccountId();

            var response = service.exists(accountId);

            assertThat(response).isEqualTo(false);
        }

        @Test
        void returns_true_when_account_exists() {
            var account = dummyAccount().build();

            repository.insert(account);

            var response = service.exists(account.getId());

            assertThat(response).isEqualTo(true);
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
            var account = createAccount(userId);

            service.blockUserAccount(userId, account.getId());

            assertStateIs(userId, account.getId(), BLOCKED);
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
            var account = createAccount(userId);
            service.blockUserAccount(userId, account.getId());

            service.reopenUserAccount(userId, account.getId());

            assertStateIs(userId, account.getId(), OPEN);
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
            var account = createAccount(userId);

            service.closeUserAccount(userId, account.getId());

            assertStateIs(userId, account.getId(), CLOSED);
        }
    }

    private Account createAccount(UserId userId) {
        return createAccount(dummyAccount().userId(userId));
    }

    private Account createAccount(Account.AccountBuilder builder) {
        var account = builder.build();

        repository.insert(account);

        return account;
    }

    private void assertStateIs(UserId userId, AccountId accountId, AccountState state) {
        assertThat(repository.findUserAccount(userId, accountId))
                .get()
                .returns(state, Account::getState);
    }
}