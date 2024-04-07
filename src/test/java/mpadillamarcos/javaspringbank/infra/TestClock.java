package mpadillamarcos.javaspringbank.infra;

import mpadillamarcos.javaspringbank.domain.time.Clock;

import java.time.Instant;

public class TestClock implements Clock {

    public static final Instant NOW = Instant.now();

    @Override
    public Instant now() {
        return NOW;
    }
}
