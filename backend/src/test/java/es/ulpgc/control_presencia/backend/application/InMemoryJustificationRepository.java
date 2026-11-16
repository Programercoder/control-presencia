package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.port.out.JustificationRepository;
import es.ulpgc.control_presencia.backend.domain.Justification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InMemoryJustificationRepository implements JustificationRepository {
    private final List<Justification> data = new ArrayList<>();

    @Override
    public List<Justification> findByPersonAndDate(String personId, LocalDate date) {
        return data.stream().filter(j -> j.personId().equals(personId) && j.date().equals(date)).toList();
    }

    @Override
    public List<Justification> findBetween(String personId, LocalDate from, LocalDate to) {
        return data.stream()
                .filter(j -> j.personId().equals(personId) && !j.date().isBefore(from) && !j.date().isAfter(to))
                .toList();
    }

    @Override
    public void save(Justification justification) {
        data.add(justification);
    }
}
