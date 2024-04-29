package mpadillamarcos.javaspringbank.domain.transaction;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyTransferRequestBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferRequestTest {

    @MethodSource("transfersWithMissingData")
    @ParameterizedTest(name = "{0}")
    void requires_mandatory_fields(String field, TransferRequest.TransferRequestBuilder builder) {
        var exception = assertThrows(IllegalArgumentException.class, builder::build);

        assertThat(exception).hasMessage(field + " must not be null");
    }

    static List<Arguments> transfersWithMissingData() {
        return List.of(
                Arguments.arguments("originAccountId", dummyTransferRequestBuilder().originAccountId(null)),
                Arguments.arguments("userId", dummyTransferRequestBuilder().userId(null)),
                Arguments.arguments("amount", dummyTransferRequestBuilder().amount(null)),
                Arguments.arguments("destinationAccountId", dummyTransferRequestBuilder().destinationAccountId(null))
        );
    }
}
