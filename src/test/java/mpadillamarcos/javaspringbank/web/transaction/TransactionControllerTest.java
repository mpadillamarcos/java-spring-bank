package mpadillamarcos.javaspringbank.web.transaction;

import mpadillamarcos.javaspringbank.domain.transaction.TransactionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyTransfer;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.money.Money.eur;
import static mpadillamarcos.javaspringbank.domain.transaction.DepositRequest.depositRequest;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.randomTransactionId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransferRequest.transferRequest;
import static mpadillamarcos.javaspringbank.domain.transaction.WithdrawRequest.withdrawRequest;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TransactionService transactionService;

    @Nested
    class Transfer {

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
            var concept = "some concept";

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
                    .transfer(transferRequest()
                            .amount(amount)
                            .destinationAccountId(destinationAccountId)
                            .originAccountId(originAccountId)
                            .userId(userId)
                            .concept(concept)
                            .build()
                    );
        }
    }

    @Nested
    class Withdraw {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/3/accounts/e095d288-9456-491d-b3a2-94c6d2d79d9b/withdraw"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/e095d288-9456-491d-b3a2-94c6d2d79d9b/accounts/6/withdraw"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_body_is_null() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/withdraw")
                            .content("{}")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_all_required_parameters_are_valid() throws Exception {
            var userId = randomUserId();
            var accountId = randomAccountId();
            var amount = eur(100);
            var concept = "some concept";

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
                            "/users/{userId}/accounts/{accountId}/withdraw",
                            userId.value(),
                            accountId.value())
                            .content(requestBody)
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(transactionService, times(1))
                    .withdraw(withdrawRequest()
                            .userId(userId)
                            .accountId(accountId)
                            .amount(amount)
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
            var concept = "some concept";

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
                    .deposit(depositRequest()
                            .userId(userId)
                            .accountId(accountId)
                            .amount(amount)
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

    @Nested
    class RejectTransaction {

        @Test
        void returns_bad_request_when_transaction_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/transactions/5/reject"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_all_parameters_are_valid() throws Exception {
            var transactionId = randomTransactionId();

            mockMvc.perform(post(
                            "/transactions/{transactionId}/reject", transactionId.value()))
                    .andExpect(status().isOk());

            verify(transactionService, times(1))
                    .reject(transactionId);
        }
    }

    @Nested
    class ListTransactions {

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(get("/accounts/5/transactions"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_transaction_list_when_all_parameters_are_valid() throws Exception {
            var transaction = dummyTransfer()
                    .amount(eur(100))
                    .build();
            var accountId = transaction.getAccountId();

            mockMvc.perform(get(
                            "/accounts/{accountId}/transactions", accountId.value()))
                    .andExpect(status().isOk());

            when(transactionService.listTransactionsByAccountId(accountId))
                    .thenReturn(List.of(transaction));

            mockMvc.perform(get("/accounts/{accountId}/transactions", accountId.value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id", equalTo(transaction.getId().value().toString())))
                    .andExpect(jsonPath("$[0].userId", equalTo(transaction.getUserId().value().toString())))
                    .andExpect(jsonPath("$[0].accountId", equalTo(accountId.value().toString())))
                    .andExpect(jsonPath("$[0].amount.amount", equalTo(100.0D)))
                    .andExpect(jsonPath("$[0].amount.currency", equalTo("EUR")))
                    .andExpect(jsonPath("$[0].createdDate", equalTo(transaction.getCreatedDate().toString())))
                    .andExpect(jsonPath("$[0].state", equalTo(transaction.getState().toString())))
                    .andExpect(jsonPath("$[0].direction", equalTo(transaction.getDirection().toString())))
                    .andExpect(jsonPath("$[0].type", equalTo(transaction.getType().toString())))
                    .andExpect(jsonPath("$[0].concept", equalTo(transaction.getConcept())));
        }
    }
}