package mpadillamarcos.javaspringbank.web.account;

import mpadillamarcos.javaspringbank.domain.account.AccountService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccount;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        void returns_ok_when_user_id_is_a_valid_uuid() throws Exception {
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
        void returns_ok_when_user_id_is_a_valid_uuid() throws Exception {
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

}