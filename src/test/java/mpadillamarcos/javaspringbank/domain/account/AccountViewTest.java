package mpadillamarcos.javaspringbank.domain.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Supplier;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccount;
import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccountAccess;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AccountViewTest {

    @MethodSource("accountViewsWithMissingData")
    @ParameterizedTest(name = "{0}")
    void requires_mandatory_fields(String field, Supplier<AccountView> supplier) {
        var exception = assertThrows(IllegalArgumentException.class, supplier::get);

        assertThat(exception).hasMessage(field + " must not be null");
    }

    @Test
    void creates_account_view_from_account_and_access_information() {
        var account = dummyAccount().build();
        var access = dummyAccountAccess().build();

        var accountView = new AccountView(account, access);

        assertThat(accountView)
                .returns(account.getId(), AccountView::getAccountId)
                .returns(access.getUserId(), AccountView::getUserId)
                .returns(account.getCreatedDate(), AccountView::getCreatedDate)
                .returns(account.getState(), AccountView::getState)
                .returns(access.getType(), AccountView::getType);
    }

    static List<Arguments> accountViewsWithMissingData() {
        return List.of(
                arguments("account", (Supplier<AccountView>) () -> new AccountView(null, dummyAccountAccess().build())),
                arguments("access", (Supplier<AccountView>) () -> new AccountView(dummyAccount().build(), null))
        );
    }
}