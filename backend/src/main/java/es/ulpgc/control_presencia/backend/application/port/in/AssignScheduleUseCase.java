package es.ulpgc.control_presencia.backend.application.port.in;

import es.ulpgc.control_presencia.backend.domain.Assignment;
import java.time.LocalDate;

public interface AssignScheduleUseCase {
    Assignment assign(String personId, String scheduleId, LocalDate from, LocalDate to);
}
