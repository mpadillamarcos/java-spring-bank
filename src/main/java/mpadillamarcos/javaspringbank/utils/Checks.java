package mpadillamarcos.javaspringbank.utils;

public class Checks {

    public static <T> T require(String name, T value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return value;
    }
}
