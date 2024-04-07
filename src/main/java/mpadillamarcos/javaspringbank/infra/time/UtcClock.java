package mpadillamarcos.javaspringbank.infra.time;

import mpadillamarcos.javaspringbank.domain.time.Clock;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UtcClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
