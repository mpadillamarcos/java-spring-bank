package mpadillamarcos.javaspringbank.infra.type;

import mpadillamarcos.javaspringbank.domain.transaction.TransactionId;
import org.springframework.stereotype.Component;

@Component
public class TransactionIdTypeHandler extends IdTypeHandler<TransactionId> {
    public TransactionIdTypeHandler() {
        super(TransactionId::new);
    }
}
