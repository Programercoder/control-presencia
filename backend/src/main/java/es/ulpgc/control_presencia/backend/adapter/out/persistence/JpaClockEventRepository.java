package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import es.ulpgc.control_presencia.backend.application.port.out.ClockEventRepository;
import es.ulpgc.control_presencia.backend.domain.ClockEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JpaClockEventRepository implements ClockEventRepository {
    private final SpringDataClockEventRepository jpa;

    public JpaClockEventRepository(SpringDataClockEventRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<ClockEvent> findLast(String personId) {
        return jpa.findFirstByPersonIdOrderByTimestampDescIdDesc(personId).map(this::toDomain);
    }

    @Override
    public List<ClockEvent> findBetween(String personId, LocalDateTime from, LocalDateTime to) {
        return jpa.findByPersonIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAscIdAsc(
                personId, from, to).stream().map(this::toDomain).toList();
    }

    @Override
    public void save(ClockEvent e) {
        jpa.save(new ClockEventEntity(e.personId(), e.timestamp(), e.type(), e.source()));
    }

    private ClockEvent toDomain(ClockEventEntity e) {
        return new ClockEvent(e.getPersonId(), e.getTimestamp(), e.getType(), e.getSource());
    }
}
