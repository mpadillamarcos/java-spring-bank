package mpadillamarcos.javaspringbank.domain.access;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static java.time.Instant.now;
import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccountAccess;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.GRANTED;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.REVOKED;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.*;
import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.newAccountAccess;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountAccessTest {

    @MethodSource("accountAccessWithMissingData")
    @ParameterizedTest(name = "{0}")
    void requires_mandatory_fields(String field, AccountAccess.AccountAccessBuilder builder) {
        var exception = assertThrows(IllegalArgumentException.class, builder::build);

        assertThat(exception).hasMessage(field + " must not be null");
    }

    @Test
    void creates_account_access_with_builder_values() {
        var accountId = randomAccountId();
        var userId = randomUserId();
        var accessType = VIEWER;
        var createdDate = now();
        var accessState = REVOKED;

        var accountAccess = newAccountAccess()
                .accountId(accountId)
                .userId(userId)
                .type(accessType)
                .createdDate(createdDate)
                .state(accessState)
                .build();

        assertThat(accountAccess)
                .returns(accountId, AccountAccess::getAccountId)
                .returns(userId, AccountAccess::getUserId)
                .returns(accessType, AccountAccess::getType)
                .returns(createdDate, AccountAccess::getCreatedDate)
                .returns(accessState, AccountAccess::getState);
    }

    @Test
    void sets_provided_type_when_granting_access() {
        var accountAccess = dummyAccountAccess().type(VIEWER).build();

        var operatorAccess = accountAccess.grant(OPERATOR);

        assertThat(operatorAccess.getType()).isEqualTo(OPERATOR);

    }

    @Test
    void throws_exception_when_changing_access_type_to_an_owner() {
        var accountAccess = dummyAccountAccess().type(OWNER).build();

        var exception = assertThrows(IllegalArgumentException.class, () -> accountAccess.grant(VIEWER));

        assertThat(exception).hasMessage("Owner access cannot be changed to another access type");
    }

    @Test
    void throws_exception_when_changing_access_type_to_owner() {
        var accountAccess = dummyAccountAccess().type(VIEWER).build();

        var exception = assertThrows(IllegalArgumentException.class, () -> accountAccess.grant(OWNER));

        assertThat(exception).hasMessage("Access type cannot be upgraded to owner");
    }

    @Test
    void does_nothing_when_giving_an_access_that_user_already_has() {
        var accountAccess = dummyAccountAccess().type(OPERATOR).build();

        var operatorAccess = accountAccess.grant(OPERATOR);

        assertThat(operatorAccess).isSameAs(accountAccess);
    }

    @Test
    void changes_state_to_granted_if_it_was_revoked() {
        var accountAccess = dummyAccountAccess().type(OPERATOR).state(REVOKED).build();

        var grantedAccountAccess = accountAccess.grant(OPERATOR);

        assertThat(grantedAccountAccess.getState()).isEqualTo(GRANTED);
    }

    @Test
    void does_nothing_when_revoking_access_to_user_with_access_revoked() {
        var accountAccess = dummyAccountAccess().state(REVOKED).build();

        var revokeAccess = accountAccess.revoke();

        assertThat(revokeAccess).isSameAs(accountAccess);
    }

    @Test
    void changes_state_to_revoked() {
        var accountAccess = dummyAccountAccess().type(VIEWER).build();

        var revokeAccess = accountAccess.revoke();

        assertThat(revokeAccess.getState()).isEqualTo(REVOKED);
    }

    @Test
    void returns_true_when_user_has_operation_permits() {
        var accountAccess = dummyAccountAccess().state(GRANTED).type(OPERATOR).build();

        assertThat(accountAccess.canOperate()).isTrue();
    }

    @Test
    void returns_false_when_user_access_is_revoked() {
        var accountAccess = dummyAccountAccess().state(REVOKED).type(OPERATOR).build();

        assertThat(accountAccess.canOperate()).isFalse();
    }

    @Test
    void returns_false_when_user_access_type_is_viewer() {
        var accountAccess = dummyAccountAccess().state(GRANTED).type(VIEWER).build();

        assertThat(accountAccess.canOperate()).isFalse();
    }

    static List<Arguments> accountAccessWithMissingData() {
        return List.of(
                Arguments.arguments("accountId", dummyAccountAccess().accountId(null)),
                Arguments.arguments("type", dummyAccountAccess().type(null)),
                Arguments.arguments("createdDate", dummyAccountAccess().createdDate(null)),
                Arguments.arguments("userId", dummyAccountAccess().userId(null)),
                Arguments.arguments("state", dummyAccountAccess().state(null))
        );
    }

}