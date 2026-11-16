package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import es.ulpgc.control_presencia.backend.application.port.out.JustificationRepository;
import es.ulpgc.control_presencia.backend.domain.Justification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class JpaJustificationRepository implements JustificationRepository {
    private final SpringDataJustificationRepository jpa;

    public JpaJustificationRepository(SpringDataJustificationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Justification> findByPersonAndDate(String personId, LocalDate date) {
        return jpa.findByPersonIdAndIncidentDateOrderByIdAsc(personId, date).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Justification> findBetween(String personId, LocalDate from, LocalDate to) {
        return jpa.findByPersonIdAndIncidentDateBetweenOrderByIncidentDateAscIdAsc(personId, from, to)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void save(Justification j) {
        jpa.save(new JustificationEntity(j.personId(), j.date(), j.incidentType(), j.reason(), j.author(),
                j.recordedAt()));
    }

    private Justification toDomain(JustificationEntity e) {
        return new Justification(e.getPersonId(), e.getIncidentDate(), e.getIncidentType(), e.getReason(),
                e.getAuthor(), e.getRecordedAt());
    }
}
