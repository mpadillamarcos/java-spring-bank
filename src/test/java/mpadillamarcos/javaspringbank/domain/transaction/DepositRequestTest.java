package mpadillamarcos.javaspringbank.domain.transaction;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyDepositRequestBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DepositRequestTest {

    @MethodSource("depositsWithMissingData")
    @ParameterizedTest(name = "{0}")
    void requires_mandatory_fields(String field, DepositRequest.DepositRequestBuilder builder) {
        var exception = assertThrows(IllegalArgumentException.class, builder::build);

        assertThat(exception).hasMessage(field + " must not be null");
    }

    static List<Arguments> depositsWithMissingData() {
        return List.of(
                Arguments.arguments("accountId", dummyDepositRequestBuilder().accountId(null)),
                Arguments.arguments("userId", dummyDepositRequestBuilder().userId(null)),
                Arguments.arguments("amount", dummyDepositRequestBuilder().amount(null))
        );
    }
}
