package mpadillamarcos.javaspringbank.utils;

import java.util.Arrays;

import static java.util.Arrays.stream;

public class Checks {

    public static <T> T require(String name, T value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return value;
    }

    public static <T extends Enum<T>> void requireState(T state, T... allowed) {
        if (stream(allowed).noneMatch(state::equals)) {
            throw new IllegalStateException("expected state to be one of " + Arrays.toString(allowed) + " but was " + state);
        }
    }
}
