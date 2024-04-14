package mpadillamarcos.javaspringbank.domain.access;

import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.infra.TestClock;
import mpadillamarcos.javaspringbank.infra.access.InMemoryAccountAccessRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static mpadillamarcos.javaspringbank.domain.access.AccessState.GRANTED;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.REVOKED;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.*;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static mpadillamarcos.javaspringbank.infra.TestClock.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

class AccountAccessServiceTest {

    private final AccountAccessRepository repository = new InMemoryAccountAccessRepository();
    private final AccountAccessService service = new AccountAccessService(repository, new TestClock());

    @Nested
    class GrantAccess {

        @Test
        void creates_a_new_account_access() {
            var accountId = randomAccountId();
            var userId = randomUserId();
            var type = VIEWER;

            service.grantAccess(accountId, userId, type);

            assertThat(repository.findGrantedAccountAccess(accountId, userId))
                    .get()
                    .returns(type, AccountAccess::getType)
                    .returns(GRANTED, AccountAccess::getState)
                    .returns(NOW, AccountAccess::getCreatedDate);
        }

        @Test
        void updates_access_type_when_provided_a_new_one() {
            var accountId = randomAccountId();
            var userId = randomUserId();

            service.grantAccess(accountId, userId, VIEWER);
            service.grantAccess(accountId, userId, OPERATOR);

            assertThat(repository.findGrantedAccountAccess(accountId, userId))
                    .get()
                    .returns(OPERATOR, AccountAccess::getType);
        }
    }

    @Nested
    class RevokeAccess {

        @Test
        void throws_not_found_exception_when_account_access_does_not_exist() {
            var accountId = randomAccountId();
            var userId = randomUserId();

            assertThrows(NotFoundException.class, () -> service.revokeAccess(accountId, userId));
        }

        @Test
        void changes_state_to_revoked() {
            var accountId = randomAccountId();
            var userId = randomUserId();

            service.grantAccess(accountId, userId, OWNER);
            service.revokeAccess(accountId, userId);

            assertThat(repository.findAccountAccess(accountId, userId))
                    .get()
                    .returns(REVOKED, AccountAccess::getState);
        }
    }
}