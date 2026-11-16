package es.ulpgc.control_presencia.backend.application.port.in;

import es.ulpgc.control_presencia.backend.domain.Justification;

import java.time.LocalDate;
import java.util.List;

public interface ListJustificationsUseCase {
    /** Justifications of the incidents from `from` to `to`, both days included. */
    List<Justification> list(String personId, LocalDate from, LocalDate to);
}
