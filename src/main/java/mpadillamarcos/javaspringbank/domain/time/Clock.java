package mpadillamarcos.javaspringbank.domain.time;

import java.time.Instant;

public interface Clock {

    Instant now();
}
