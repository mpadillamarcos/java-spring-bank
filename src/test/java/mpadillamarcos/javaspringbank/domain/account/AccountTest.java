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
import static mpadillamarcos.javaspringbank.domain.account.AccountState.CLOSED;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void set_state_to_blocked_when_blocking_an_open_account() {
        var account = dummyAccount().build();

        var blocked = account.block();

        assertThat(blocked.getState()).isEqualTo(BLOCKED);
    }

    @Test
    void throws_exception_when_blocking_a_closed_account() {
        var account = dummyAccount().state(CLOSED).build();

        var exception = assertThrows(IllegalStateException.class, account::block);

        assertThat(exception).hasMessage("expected state to be one of [OPEN] but was CLOSED");
    }

    @Test
    void does_nothing_when_blocking_a_blocked_account() {
        var account = dummyAccount().state(BLOCKED).build();

        var blocked = account.block();

        assertThat(blocked).isSameAs(account);
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