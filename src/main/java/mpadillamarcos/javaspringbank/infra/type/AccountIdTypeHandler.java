package mpadillamarcos.javaspringbank.infra.type;

import mpadillamarcos.javaspringbank.domain.account.AccountId;
import org.springframework.stereotype.Component;

@Component
public class AccountIdTypeHandler extends IdTypeHandler<AccountId> {

    public AccountIdTypeHandler() {
        super(AccountId::new);
    }
}