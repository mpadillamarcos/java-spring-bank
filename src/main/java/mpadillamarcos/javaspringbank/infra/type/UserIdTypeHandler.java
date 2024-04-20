package mpadillamarcos.javaspringbank.infra.type;

import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class UserIdTypeHandler extends IdTypeHandler<UserId> {

    public UserIdTypeHandler() {
        super(UserId::new);
    }
}