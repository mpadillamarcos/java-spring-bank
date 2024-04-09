package mpadillamarcos.javaspringbank.domain.access;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static java.time.Instant.now;
import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccountAccess;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.REVOKED;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.VIEWER;
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