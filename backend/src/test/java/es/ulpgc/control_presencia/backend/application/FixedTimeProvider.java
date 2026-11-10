package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.port.out.TimeProvider;

import java.time.LocalDateTime;

public class FixedTimeProvider implements TimeProvider {
    private LocalDateTime now;

    public FixedTimeProvider(LocalDateTime now) {
        this.now = now;
    }

    public void set(LocalDateTime now) {
        this.now = now;
    }

    @Override
    public LocalDateTime now() {
        return now;
    }
}
