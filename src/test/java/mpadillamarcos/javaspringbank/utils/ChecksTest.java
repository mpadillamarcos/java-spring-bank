package mpadillamarcos.javaspringbank.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static mpadillamarcos.javaspringbank.utils.Checks.require;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
}