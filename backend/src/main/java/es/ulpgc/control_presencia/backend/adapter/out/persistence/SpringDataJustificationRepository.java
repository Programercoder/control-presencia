package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataJustificationRepository extends JpaRepository<JustificationEntity, Long> {
    List<JustificationEntity> findByPersonIdAndIncidentDateOrderByIdAsc(String personId, LocalDate date);

    List<JustificationEntity> findByPersonIdAndIncidentDateBetweenOrderByIncidentDateAscIdAsc(
            String personId, LocalDate from, LocalDate to);
}
