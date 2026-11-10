package es.ulpgc.control_presencia.backend.application.port.out;

import es.ulpgc.control_presencia.backend.domain.Schedule;
import java.util.Optional;

public interface ScheduleRepository {
    Optional<Schedule> findById(String id);
    void save(String id, Schedule schedule);
}