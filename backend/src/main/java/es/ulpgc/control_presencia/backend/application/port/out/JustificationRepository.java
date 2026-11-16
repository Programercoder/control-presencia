package es.ulpgc.control_presencia.backend.application.port.out;

import es.ulpgc.control_presencia.backend.domain.Justification;

import java.time.LocalDate;
import java.util.List;

public interface JustificationRepository {
    List<Justification> findByPersonAndDate(String personId, LocalDate date);
    List<Justification> findBetween(String personId, LocalDate from, LocalDate to);
    void save(Justification justification);   // append-only: no update or delete
}
