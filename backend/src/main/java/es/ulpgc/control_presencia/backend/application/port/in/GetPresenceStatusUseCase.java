package es.ulpgc.control_presencia.backend.application.port.in;

import es.ulpgc.control_presencia.backend.domain.PresenceResult;
import java.time.LocalDate;

public interface GetPresenceStatusUseCase {
    PresenceResult getStatus(String personId, LocalDate date);
}
