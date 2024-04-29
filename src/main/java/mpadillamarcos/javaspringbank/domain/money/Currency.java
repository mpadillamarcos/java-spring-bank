package mpadillamarcos.javaspringbank.domain.money;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {

    EUR("€"), JPY("¥");

    private final String symbol;

    public String iso3() {
        return name();
    }
}
