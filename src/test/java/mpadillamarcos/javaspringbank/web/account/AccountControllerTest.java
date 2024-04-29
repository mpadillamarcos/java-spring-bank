package mpadillamarcos.javaspringbank.web.account;

import mpadillamarcos.javaspringbank.domain.account.AccountService;
import mpadillamarcos.javaspringbank.domain.account.AccountView;
import mpadillamarcos.javaspringbank.domain.money.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static mpadillamarcos.javaspringbank.domain.Instances.*;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;
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
            var access = dummyAccountAccess().userId(account.getUserId()).build();
            var balance = dummyBalance().amount(Money.eur(33)).accountId(account.getId()).build();
            var userId = account.getUserId().value();
            var accountId = account.getId().value();

            when(accountService.openAccount(account.getUserId()))
                    .thenReturn(new AccountView(account, access, balance));

            mockMvc.perform(post("/users/{userId}/accounts", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(accountId.toString())))
                    .andExpect(jsonPath("$.userId", equalTo(userId.toString())))
                    .andExpect(jsonPath("$.createdDate", equalTo(account.getCreatedDate().toString())))
                    .andExpect(jsonPath("$.balance.amount", equalTo(33D)))
                    .andExpect(jsonPath("$.balance.currency", equalTo("EUR")))
                    .andExpect(jsonPath("$.state", equalTo("OPEN")))
                    .andExpect(jsonPath("$.accessType", equalTo("OWNER")));
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
            var accountId = account.getId();
            var access = dummyAccountAccess().accountId(accountId).userId(account.getUserId()).type(OWNER).build();
            var balance = dummyBalance().amount(Money.eur(200)).accountId(accountId).build();
            var accountView = new AccountView(account, access, balance);

            when(accountService.listUserAccounts(userId(userId)))
                    .thenReturn(List.of(accountView));

            mockMvc.perform(get("/users/{userId}/accounts", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id", equalTo(accountId.value().toString())))
                    .andExpect(jsonPath("$[0].userId", equalTo(userId.toString())))
                    .andExpect(jsonPath("$[0].createdDate", equalTo(account.getCreatedDate().toString())))
                    .andExpect(jsonPath("$[0].balance.amount", equalTo(200D)))
                    .andExpect(jsonPath("$[0].balance.currency", equalTo("EUR")))
                    .andExpect(jsonPath("$[0].state", equalTo("OPEN")))
                    .andExpect(jsonPath("$[0].accessType", equalTo("OWNER")));
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
            var userId = account.getUserId();
            var accountId = account.getId();
            var access = dummyAccountAccess().accountId(accountId).userId(userId).build();
            var balance = dummyBalance().amount(Money.eur(150)).accountId(accountId).build();
            var accountView = new AccountView(account, access, balance);

            when(accountService.findAccountView(account.getUserId(), account.getId()))
                    .thenReturn(Optional.of(accountView));

            mockMvc.perform(get("/users/{userId}/accounts/{accountId}", userId.value(), accountId.value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(accountId.value().toString())))
                    .andExpect(jsonPath("$.userId", equalTo(userId.value().toString())))
                    .andExpect(jsonPath("$.createdDate", equalTo(account.getCreatedDate().toString())))
                    .andExpect(jsonPath("$.balance.amount", equalTo(150D)))
                    .andExpect(jsonPath("$.balance.currency", equalTo("EUR")))
                    .andExpect(jsonPath("$.accessType", equalTo("OWNER")))
                    .andExpect(jsonPath("$.state", equalTo("OPEN")));
        }

        @Test
        void returns_not_found_when_account_id_does_not_exist() throws Exception {
            var userId = randomUserId();
            var accountId = randomAccountId();

            when(accountService.findAccountView(userId, accountId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/users/{userId}/accounts/{accountId}", userId.value(), accountId.value()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", equalTo("account not found")));
        }
    }

    @Nested
    class BlockAccount {

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/accounts/5/block"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_blocking_user_account() throws Exception {
            var accountId = randomAccountId();

            mockMvc.perform(post("/accounts/{accountId}/block", accountId.value()))
                    .andExpect(status().isOk());

            verify(accountService).blockAccount(accountId);
        }
    }

    @Nested
    class UnblockAccount {

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/accounts/5/unblock"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_reopening_user_account() throws Exception {
            var accountId = randomAccountId();

            mockMvc.perform(post("/accounts/{accountId}/unblock", accountId.value()))
                    .andExpect(status().isOk());

            verify(accountService).unblockAccount(accountId);
        }
    }

    @Nested
    class CloseAccount {

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(delete("/accounts/5"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_closing_user_account() throws Exception {
            var accountId = randomAccountId();

            mockMvc.perform(delete("/accounts/{accountId}", accountId.value()))
                    .andExpect(status().isOk());

            verify(accountService).closeAccount(accountId);
        }
    }
}