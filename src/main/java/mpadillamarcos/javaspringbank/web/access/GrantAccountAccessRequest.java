package mpadillamarcos.javaspringbank.web.access;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mpadillamarcos.javaspringbank.domain.access.AccessType;

@Data
public class GrantAccountAccessRequest {

    @NotNull
    private AccessType type;
}
