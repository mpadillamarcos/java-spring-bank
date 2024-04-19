package mpadillamarcos.javaspringbank.web.transaction;

import mpadillamarcos.javaspringbank.domain.transaction.TransactionRequest;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.money.Money.eur;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.randomTransactionId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.*;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TransactionService transactionService;

    @Nested
    class CreateTransfer {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/3/accounts/e095d288-9456-491d-b3a2-94c6d2d79d9b/transfers"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/e095d288-9456-491d-b3a2-94c6d2d79d9b/accounts/6/transfers"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_body_is_null() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/transfers")
                            .content("{}")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_destination_account_id_is_invalid() throws Exception {
            var destinationAccountId = 3;
            var amount = eur(100);

            String requestBody = String.format(
                    """
                            {
                                "destinationAccountId": "%s",
                                "amount": {
                                    "amount": %s,
                                    "currency": "EUR"
                                }
                            }
                            """,
                    destinationAccountId,
                    amount.getAmount()
            );
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/transfers")
                            .content(requestBody)
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_all_required_parameters_are_valid() throws Exception {
            var userId = randomUserId();
            var originAccountId = randomAccountId();
            var destinationAccountId = randomAccountId();
            var amount = eur(100);
            var concept = "";

            String requestBody = String.format(
                    """
                            {
                                "destinationAccountId": "%s",
                                "amount": {
                                    "amount": %s,
                                    "currency": "EUR"
                                },
                                "concept": "%s"
                            }
                            """,
                    destinationAccountId.value(),
                    amount.getAmount(),
                    concept
            );

            mockMvc.perform(post(
                            "/users/{userId}/accounts/{originAccountId}/transfers",
                            userId.value(),
                            originAccountId.value())
                            .content(requestBody)
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(transactionService, times(1))
                    .createTransfer(TransactionRequest.builder()
                            .userId(userId)
                            .originAccountId(originAccountId)
                            .destinationAccountId(destinationAccountId)
                            .amount(amount)
                            .type(TRANSFER)
                            .concept(concept)
                            .build());
        }
    }

    @Nested
    class Withdraw {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/3/accounts/e095d288-9456-491d-b3a2-94c6d2d79d9b/withdrawals"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/e095d288-9456-491d-b3a2-94c6d2d79d9b/accounts/6/withdrawals"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_body_is_null() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/withdrawals")
                            .content("{}")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_all_required_parameters_are_valid() throws Exception {
            var userId = randomUserId();
            var accountId = randomAccountId();
            var amount = eur(100);
            var concept = "";

            String requestBody = String.format(
                    """
                                {
                                    "amount": {
                                        "amount": %s,
                                        "currency": "EUR"
                                    },
                                    "concept": "%s"
                                }
                            """,
                    amount.getAmount(),
                    concept
            );

            mockMvc.perform(post(
                            "/users/{userId}/accounts/{accountId}/withdrawals",
                            userId.value(),
                            accountId.value())
                            .content(requestBody)
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(transactionService, times(1))
                    .withdraw(TransactionRequest.builder()
                            .userId(userId)
                            .originAccountId(accountId)
                            .amount(amount)
                            .type(WITHDRAW)
                            .concept(concept)
                            .build());
        }
    }

    @Nested
    class Deposit {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/3/accounts/e095d288-9456-491d-b3a2-94c6d2d79d9b/deposits"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/e095d288-9456-491d-b3a2-94c6d2d79d9b/accounts/6/deposits"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_body_is_null() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/deposits")
                            .content("{}")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_all_required_parameters_are_valid() throws Exception {
            var userId = randomUserId();
            var accountId = randomAccountId();
            var amount = eur(100);
            var concept = "";

            String requestBody = String.format(
                    """
                                {
                                    "amount": {
                                        "amount": %s,
                                        "currency": "EUR"
                                    },
                                    "concept": "%s"
                                }
                            """,
                    amount.getAmount(),
                    concept
            );

            mockMvc.perform(post(
                            "/users/{userId}/accounts/{accountId}/deposits",
                            userId.value(),
                            accountId.value())
                            .content(requestBody)
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(transactionService, times(1))
                    .deposit(TransactionRequest.builder()
                            .userId(userId)
                            .originAccountId(accountId)
                            .amount(amount)
                            .type(DEPOSIT)
                            .concept(concept)
                            .build());
        }

    }

    @Nested
    class ConfirmTransaction {

        @Test
        void returns_bad_request_when_transaction_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/transactions/5/confirm"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_all_parameters_are_valid() throws Exception {
            var transactionId = randomTransactionId();

            mockMvc.perform(post(
                            "/transactions/{transactionId}/confirm", transactionId.value()))
                    .andExpect(status().isOk());

            verify(transactionService, times(1))
                    .confirmTransaction(transactionId);
        }
    }
}