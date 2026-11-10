package es.ulpgc.control_presencia.backend.application.port.out;

import es.ulpgc.control_presencia.backend.domain.Assignment;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository {
    Optional<Assignment> findActive(String personId, LocalDate date);
    List<Assignment> findByPerson(String personId);
    void save(Assignment assignment);
}
