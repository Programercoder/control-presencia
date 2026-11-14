package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import es.ulpgc.control_presencia.backend.application.port.out.AssignmentRepository;
import es.ulpgc.control_presencia.backend.domain.Assignment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class JpaAssignmentRepository implements AssignmentRepository {
    private final SpringDataAssignmentRepository jpa;

    public JpaAssignmentRepository(SpringDataAssignmentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Assignment> findActive(String personId, LocalDate date) {
        return findByPerson(personId).stream().filter(a -> a.covers(date)).findFirst();
    }

    @Override
    public List<Assignment> findByPerson(String personId) {
        return jpa.findByPersonId(personId).stream()
                .map(e -> new Assignment(e.getPersonId(), e.getScheduleId(), e.getValidFrom(), e.getValidTo()))
                .toList();
    }

    @Override
    public void save(Assignment a) {
        jpa.save(new AssignmentEntity(a.personId(), a.scheduleId(), a.from(), a.to()));
    }
}
