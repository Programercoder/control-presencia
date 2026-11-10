package es.ulpgc.control_presencia.backend.application.port.out;

import es.ulpgc.control_presencia.backend.domain.ClockEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClockEventRepository {
    Optional<ClockEvent> findLast(String personId);
    List<ClockEvent> findBetween(String personId, LocalDateTime from, LocalDateTime to);
    void save(ClockEvent event);   // append-only: no update or delete
}