package mpadillamarcos.javaspringbank.web.access;

import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccountAccess;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OPERATOR;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountAccessController.class)
class AccountAccessControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountAccessService accountAccessService;

    @Nested
    class GrantAccess {

        @Test
        void returns_bad_request_when_user_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/access"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_account_id_is_not_uuid() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/5/access"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_body_is_null() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/access")
                            .content("{}")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_bad_request_when_required_body_is_invalid() throws Exception {
            mockMvc.perform(post("/users/f01f898b-82fc-4860-acc0-76b13dcd78c5/accounts/f01f898b-82fc-4860-acc0-76b13dcd78c5/access")
                            .content("{\"type\": \"patata\"}")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns_ok_when_all_required_parameters_are_valid() throws Exception {
            var accountAccess = dummyAccountAccess().type(OPERATOR).build();
            var accountId = accountAccess.getAccountId().value();
            var userId = accountAccess.getUserId().value();

            mockMvc.perform(post("/users/{userId}/accounts/{accountId}/access", userId, accountId)
                            .content("{\"type\": \"OPERATOR\"}")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(accountAccessService, times(1))
                    .grantAccess(accountAccess.getAccountId(), accountAccess.getUserId(), accountAccess.getType());
        }
    }

}