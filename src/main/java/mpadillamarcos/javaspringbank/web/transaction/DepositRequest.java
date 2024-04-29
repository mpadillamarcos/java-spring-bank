package mpadillamarcos.javaspringbank.web.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mpadillamarcos.javaspringbank.domain.money.Money;

@Data
public class DepositRequest {

    @NotNull
    private Money amount;

    private String concept;
}
