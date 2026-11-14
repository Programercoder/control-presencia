package es.ulpgc.control_presencia.backend.adapter.out.time;

import es.ulpgc.control_presencia.backend.application.port.out.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SystemTimeProvider implements TimeProvider {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}