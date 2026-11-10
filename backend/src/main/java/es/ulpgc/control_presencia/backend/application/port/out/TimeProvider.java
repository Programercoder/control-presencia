package es.ulpgc.control_presencia.backend.application.port.out;

import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDateTime now();
}
