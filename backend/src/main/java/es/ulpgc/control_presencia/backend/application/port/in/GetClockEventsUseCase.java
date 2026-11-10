package es.ulpgc.control_presencia.backend.application.port.in;

import es.ulpgc.control_presencia.backend.domain.ClockEvent;
import java.time.LocalDate;
import java.util.List;

public interface GetClockEventsUseCase {
    /** Events from `from` to `to`, both days included. */
    List<ClockEvent> getEvents(String personId, LocalDate from, LocalDate to);
}
