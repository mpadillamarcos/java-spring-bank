package mpadillamarcos.javaspringbank.domain.access;

import mpadillamarcos.javaspringbank.infra.TestClock;
import mpadillamarcos.javaspringbank.infra.access.InMemoryAccountAccessRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static mpadillamarcos.javaspringbank.domain.access.AccessState.GRANTED;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OPERATOR;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.VIEWER;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static mpadillamarcos.javaspringbank.infra.TestClock.NOW;
import static org.assertj.core.api.Assertions.assertThat;

class AccountAccessServiceTest {

    private final AccountAccessRepository repository = new InMemoryAccountAccessRepository();
    private final AccountAccessService service = new AccountAccessService(repository, new TestClock());

    @Nested
    class GrantAccess {

        @Test
        void creates_a_new_account_access() {
            var userId = randomUserId();
            var accountId = randomAccountId();
            var type = VIEWER;

            service.grantAccess(accountId, userId, type);

            assertThat(repository.findAccountAccess(accountId, userId))
                    .get()
                    .returns(type, AccountAccess::getType)
                    .returns(GRANTED, AccountAccess::getState)
                    .returns(NOW, AccountAccess::getCreatedDate);
        }

        @Test
        void updates_access_type_when_provided_a_new_one() {
            var userId = randomUserId();
            var accountId = randomAccountId();

            service.grantAccess(accountId, userId, VIEWER);
            service.grantAccess(accountId, userId, OPERATOR);

            assertThat(repository.findAccountAccess(accountId, userId))
                    .get()
                    .returns(OPERATOR, AccountAccess::getType);
        }
    }
}