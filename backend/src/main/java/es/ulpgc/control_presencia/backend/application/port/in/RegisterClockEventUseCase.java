package es.ulpgc.control_presencia.backend.application.port.in;

import es.ulpgc.control_presencia.backend.domain.ClockEvent;
import es.ulpgc.control_presencia.backend.domain.ClockEventType;
import es.ulpgc.control_presencia.backend.domain.EventSource;

public interface RegisterClockEventUseCase {
    ClockEvent register(String personId, ClockEventType type, EventSource source);
}
