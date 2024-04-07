package mpadillamarcos.javaspringbank.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static mpadillamarcos.javaspringbank.domain.account.AccountState.*;
import static mpadillamarcos.javaspringbank.utils.Checks.require;
import static mpadillamarcos.javaspringbank.utils.Checks.requireState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChecksTest {

    @Nested
    class Require {
        @Test
        void throws_illegal_argument_exception_if_value_is_null() {
            var exception = assertThrows(IllegalArgumentException.class, () -> {
                require("some field", null);
            });

            assertThat(exception).hasMessage("some field must not be null");
        }

        @Test
        void returns_value_if_not_null() {
            var value = require("some field", "some value");

            assertThat(value).isEqualTo("some value");
        }
    }

    @Nested
    class RequireState {
        @Test
        void throws_illegal_state_exception_if_value_is_not_within_allowed_values() {
            var state = OPEN;

            var exception = assertThrows(IllegalStateException.class, () -> {
                requireState(state, CLOSED, BLOCKED);
            });

            assertThat(exception).hasMessage("expected state to be one of [CLOSED, BLOCKED] but was OPEN");
        }
    }
}