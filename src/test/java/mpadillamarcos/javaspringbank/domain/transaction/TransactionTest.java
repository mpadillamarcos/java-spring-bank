package mpadillamarcos.javaspringbank.domain.transaction;

import mpadillamarcos.javaspringbank.domain.money.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.List;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyTransfer;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.transaction.Transaction.newTransaction;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.OUTGOING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.randomTransactionId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.*;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.TRANSFER;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {

    @MethodSource("transactionsWithMissingData")
    @ParameterizedTest(name = "{0}")
    void requires_mandatory_fields(String field, Transaction.TransactionBuilder builder) {
        var exception = assertThrows(IllegalArgumentException.class, builder::build);

        assertThat(exception).hasMessage(field + " must not be null");
    }

    @Test
    void creates_transaction_with_builder_values() {
        var id = randomTransactionId();
        var groupId = randomTransactionGroupId();
        var userId = randomUserId();
        var accountId = randomAccountId();
        var amount = Money.eur(1000);
        var createdDate = Instant.now();
        var state = PENDING;
        var direction = OUTGOING;
        var type = TRANSFER;

        var transaction = newTransaction()
                .id(id)
                .groupId(groupId)
                .userId(userId)
                .accountId(accountId)
                .amount(amount)
                .createdDate(createdDate)
                .state(state)
                .direction(direction)
                .type(type)
                .build();

        assertThat(transaction)
                .returns(id, Transaction::getId)
                .returns(groupId, Transaction::getGroupId)
                .returns(userId, Transaction::getUserId)
                .returns(accountId, Transaction::getAccountId)
                .returns(amount, Transaction::getAmount)
                .returns(createdDate, Transaction::getCreatedDate)
                .returns(state, Transaction::getState)
                .returns(direction, Transaction::getDirection)
                .returns(type, Transaction::getType);
    }

    @Nested
    class Confirm {

        @Test
        void sets_state_to_confirmed_when_confirming_a_pending_transaction() {
            var transaction = dummyTransfer().build();

            var confirmedTransaction = transaction.confirm();

            assertThat(confirmedTransaction.getState()).isEqualTo(CONFIRMED);
        }

        @Test
        void throws_exception_when_confirming_a_declined_transaction() {
            var transaction = dummyTransfer().state(DECLINED).build();

            var exception = assertThrows(IllegalStateException.class, transaction::confirm);

            assertThat(exception).hasMessage("expected state to be one of [PENDING] but was DECLINED");
        }

        @Test
        void does_nothing_when_confirming_a_confirmed_account() {
            var transaction = dummyTransfer().state(CONFIRMED).build();

            var confirmedTransaction = transaction.confirm();

            assertThat(confirmedTransaction).isSameAs(transaction);
        }
    }

    static List<Arguments> transactionsWithMissingData() {
        return List.of(
                Arguments.arguments("id", dummyTransfer().id(null)),
                Arguments.arguments("groupId", dummyTransfer().groupId(null)),
                Arguments.arguments("userId", dummyTransfer().userId(null)),
                Arguments.arguments("accountId", dummyTransfer().accountId(null)),
                Arguments.arguments("amount", dummyTransfer().amount(null)),
                Arguments.arguments("createdDate", dummyTransfer().createdDate(null)),
                Arguments.arguments("state", dummyTransfer().state(null)),
                Arguments.arguments("direction", dummyTransfer().direction(null)),
                Arguments.arguments("type", dummyTransfer().type(null))
        );
    }
}