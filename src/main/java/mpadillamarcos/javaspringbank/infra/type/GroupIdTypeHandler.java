package mpadillamarcos.javaspringbank.infra.type;

import mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId;
import org.springframework.stereotype.Component;

@Component
public class GroupIdTypeHandler extends IdTypeHandler<TransactionGroupId> {

    public GroupIdTypeHandler() {
        super(TransactionGroupId::new);
    }
}
