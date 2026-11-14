package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataClockEventRepository extends JpaRepository<ClockEventEntity, Long> {
    Optional<ClockEventEntity> findFirstByPersonIdOrderByTimestampDescIdDesc(String personId);

    List<ClockEventEntity> findByPersonIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAscIdAsc(
            String personId, LocalDateTime from, LocalDateTime to);
}