package mpadillamarcos.javaspringbank.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static mpadillamarcos.javaspringbank.utils.Checks.require;

@ToString
@EqualsAndHashCode
public class Id<T> {

    protected final T value;

    public Id(T value) {
        this.value = require("value", value);
    }

    public T value() {
        return value;
    }
}
