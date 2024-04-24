package mpadillamarcos.javaspringbank.web.transaction;

import mpadillamarcos.javaspringbank.domain.transaction.DepositRequest;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionService;
import mpadillamarcos.javaspringbank.domain.transaction.WithdrawRequest;
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
import static mpadillamarcos.javaspringbank.domain.transaction.TransferRequest.transferRequestBuilder;
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
            mockMvc.perform(post("/users/3/accounts/e095d288-9456-491d-b3a2-94c6d2d79d9b/transfer"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/e095d288-9456-491d-b3a2-94c6d2d79d9b/accounts/6/transfer"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_body_is_null() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/transfer")
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
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/transfer")
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
                            "/users/{userId}/accounts/{originAccountId}/transfer",
                            userId.value(),
                            originAccountId.value())
                            .content(requestBody)
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(transactionService, times(1))
                    .transfer(transferRequestBuilder()
                            .amount(amount)
                            .destinationAccountId(destinationAccountId)
                            .originAccountId(originAccountId)
                            .userId(userId)
                            .type(TRANSFER)
                            .concept(concept)
                            .build()
                    );
        }
    }

    @Nested
    class Withdraw {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/3/accounts/e095d288-9456-491d-b3a2-94c6d2d79d9b/withdrawal"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/e095d288-9456-491d-b3a2-94c6d2d79d9b/accounts/6/withdrawal"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_body_is_null() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/withdrawal")
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
                            "/users/{userId}/accounts/{accountId}/withdrawal",
                            userId.value(),
                            accountId.value())
                            .content(requestBody)
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(transactionService, times(1))
                    .withdraw(WithdrawRequest.withdrawRequestBuilder()
                            .userId(userId)
                            .accountId(accountId)
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
            mockMvc.perform(post("/users/3/accounts/e095d288-9456-491d-b3a2-94c6d2d79d9b/deposit"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/e095d288-9456-491d-b3a2-94c6d2d79d9b/accounts/6/deposit"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_body_is_null() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/deposit")
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
                            "/users/{userId}/accounts/{accountId}/deposit",
                            userId.value(),
                            accountId.value())
                            .content(requestBody)
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(transactionService, times(1))
                    .deposit(DepositRequest.depositRequestBuilder()
                            .userId(userId)
                            .accountId(accountId)
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
                    .confirm(transactionId);
        }
    }
}