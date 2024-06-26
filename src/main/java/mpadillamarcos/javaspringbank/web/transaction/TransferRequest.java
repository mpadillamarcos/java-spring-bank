package mpadillamarcos.javaspringbank.web.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mpadillamarcos.javaspringbank.domain.money.Money;

import java.util.UUID;

@Data
public class TransferRequest {

    @NotNull
    private Money amount;

    @NotNull
    private UUID destinationAccountId;

    private String concept;
}
