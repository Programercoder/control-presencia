package es.ulpgc.control_presencia.backend.application.port.in;

import es.ulpgc.control_presencia.backend.domain.Schedule;

import java.util.Map;

public interface ListSchedulesUseCase {
    /** Schedule id -> schedule. */
    Map<String, Schedule> list();
}
