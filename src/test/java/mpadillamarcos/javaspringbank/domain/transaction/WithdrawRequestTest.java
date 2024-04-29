package mpadillamarcos.javaspringbank.domain.transaction;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyWithdrawRequestBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WithdrawRequestTest {

    @MethodSource("withdrawalsWithMissingData")
    @ParameterizedTest(name = "{0}")
    void requires_mandatory_fields(String field, WithdrawRequest.WithdrawRequestBuilder builder) {
        var exception = assertThrows(IllegalArgumentException.class, builder::build);

        assertThat(exception).hasMessage(field + " must not be null");
    }

    static List<Arguments> withdrawalsWithMissingData() {
        return List.of(
                Arguments.arguments("accountId", dummyWithdrawRequestBuilder().accountId(null)),
                Arguments.arguments("userId", dummyWithdrawRequestBuilder().userId(null)),
                Arguments.arguments("amount", dummyWithdrawRequestBuilder().amount(null))
        );
    }
}
