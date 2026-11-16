package es.ulpgc.control_presencia.backend.application.port.in;

import es.ulpgc.control_presencia.backend.domain.IncidentType;
import es.ulpgc.control_presencia.backend.domain.Justification;

import java.time.LocalDate;

public interface JustifyIncidentUseCase {
    Justification justify(String personId, LocalDate date, IncidentType type, String reason, String author);
}
