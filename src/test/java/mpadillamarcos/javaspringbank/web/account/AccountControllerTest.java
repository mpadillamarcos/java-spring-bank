package mpadillamarcos.javaspringbank.web.account;

import mpadillamarcos.javaspringbank.domain.account.AccountService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccount;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountService accountService;

    @Nested
    class OpenAccount {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/5/accounts"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_opened_account() throws Exception {
            var account = dummyAccount().build();
            var userId = account.getUserId().value();
            var accountId = account.getId().value();

            when(accountService.openAccount(account.getUserId()))
                    .thenReturn(account);

            mockMvc.perform(post("/users/{userId}/accounts", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(accountId.toString())))
                    .andExpect(jsonPath("$.userId", equalTo(userId.toString())))
                    .andExpect(jsonPath("$.createdDate", equalTo(account.getCreatedDate().toString())))
                    .andExpect(jsonPath("$.state", equalTo("OPEN")));
        }
    }

    @Nested
    class ListUserAccounts {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(get("/users/5/accounts"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_list_of_user_accounts() throws Exception {
            var account = dummyAccount().build();
            var userId = account.getUserId().value();
            var accountId = account.getId().value();

            when(accountService.listUserAccounts(account.getUserId()))
                    .thenReturn(List.of(account));

            mockMvc.perform(get("/users/{userId}/accounts", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id", equalTo(accountId.toString())))
                    .andExpect(jsonPath("$[0].userId", equalTo(userId.toString())))
                    .andExpect(jsonPath("$[0].createdDate", equalTo(account.getCreatedDate().toString())))
                    .andExpect(jsonPath("$[0].state", equalTo("OPEN")));
        }
    }

    @Nested
    class FindUserAccount {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(get("/users/5/accounts/e095d288-9456-491d-b3a2-94c6d2d79dbb"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(get("/users/e095d288-9456-491d-b3a2-94c6d2d79dbb/accounts/5"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_user_account_when_it_exists() throws Exception {
            var account = dummyAccount().build();
            var userId = account.getUserId().value();
            var accountId = account.getId().value();

            when(accountService.findUserAccount(account.getUserId(), account.getId()))
                    .thenReturn(Optional.of(account));

            mockMvc.perform(get("/users/{userId}/accounts/{accountId}", userId, accountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(accountId.toString())))
                    .andExpect(jsonPath("$.userId", equalTo(userId.toString())))
                    .andExpect(jsonPath("$.createdDate", equalTo(account.getCreatedDate().toString())))
                    .andExpect(jsonPath("$.state", equalTo("OPEN")));
        }

        @Test
        void returns_not_found_when_account_id_does_not_exist() throws Exception {
            var userId = randomUserId();
            var accountId = randomAccountId();

            when(accountService.findUserAccount(userId, accountId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/users/{userId}/accounts/{accountId}", userId.value(), accountId.value()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", equalTo("account not found")));
        }
    }

    @Nested
    class BlockUserAccount {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/5/accounts/e095d288-9456-491d-b3a2-94c6d2d79dbb/block"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/e095d288-9456-491d-b3a2-94c6d2d79dbb/accounts/5/block"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_blocking_user_account() throws Exception {
            var userId = randomUserId();
            var accountId = randomAccountId();

            mockMvc.perform(post("/users/{userId}/accounts/{accountId}/block", userId.value(), accountId.value()))
                    .andExpect(status().isOk());

            verify(accountService).blockUserAccount(userId, accountId);
        }
    }

    @Nested
    class ReopenUserAccount {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/5/accounts/e095d288-9456-491d-b3a2-94c6d2d79dbb/reopen"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/e095d288-9456-491d-b3a2-94c6d2d79dbb/accounts/5/reopen"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_reopening_user_account() throws Exception {
            var userId = randomUserId();
            var accountId = randomAccountId();

            mockMvc.perform(post("/users/{userId}/accounts/{accountId}/reopen", userId.value(), accountId.value()))
                    .andExpect(status().isOk());

            verify(accountService).reopenUserAccount(userId, accountId);
        }
    }

    @Nested
    class CloseUserAccount {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(delete("/users/5/accounts/e095d288-9456-491d-b3a2-94c6d2d79dbb"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(delete("/users/e095d288-9456-491d-b3a2-94c6d2d79dbb/accounts/5"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_closing_user_account() throws Exception {
            var userId = randomUserId();
            var accountId = randomAccountId();

            mockMvc.perform(delete("/users/{userId}/accounts/{accountId}", userId.value(), accountId.value()))
                    .andExpect(status().isOk());

            verify(accountService).closeUserAccount(userId, accountId);
        }
    }
}