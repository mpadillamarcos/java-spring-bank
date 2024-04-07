package mpadillamarcos.javaspringbank.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Id<T> {

    protected final T value;

    public Id(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }
}
