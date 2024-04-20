package mpadillamarcos.javaspringbank.infra;

import mpadillamarcos.javaspringbank.domain.time.Clock;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.SECONDS;

public class TestClock implements Clock {

    public static final Instant NOW = Instant.now().truncatedTo(SECONDS);

    @Override
    public Instant now() {
        return NOW;
    }
}
