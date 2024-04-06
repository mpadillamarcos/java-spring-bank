package mpadillamarcos.javaspringbank.domain.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.List;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccount;
import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.BLOCKED;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @MethodSource("accountsWithMissingData")
    @ParameterizedTest(name = "{0}")
    void requires_mandatory_fields(String field, Account.AccountBuilder builder) {
        var exception = assertThrows(IllegalArgumentException.class, builder::build);

        assertThat(exception).hasMessage(field + " must not be null");
    }

    @Test
    void creates_account_with_builder_values() {
        var id = randomAccountId();
        var userId = randomUserId();
        var state = BLOCKED;
        var createdDate = Instant.now();

        var account = newAccount()
                .id(id)
                .state(state)
                .userId(userId)
                .createdDate(createdDate)
                .build();

        assertThat(account)
                .returns(id, Account::getId)
                .returns(userId, Account::getUserId)
                .returns(state, Account::getState)
                .returns(createdDate, Account::getCreatedDate);
    }

    static List<Arguments> accountsWithMissingData() {
        return List.of(
                Arguments.arguments("id", dummyAccount().id(null)),
                Arguments.arguments("state", dummyAccount().state(null)),
                Arguments.arguments("createdDate", dummyAccount().createdDate(null)),
                Arguments.arguments("userId", dummyAccount().userId(null))
        );
    }
}