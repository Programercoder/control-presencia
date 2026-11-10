package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.port.out.AssignmentRepository;
import es.ulpgc.control_presencia.backend.domain.Assignment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryAssignmentRepository implements AssignmentRepository {
    private final List<Assignment> data = new ArrayList<>();

    @Override
    public Optional<Assignment> findActive(String personId, LocalDate date) {
        return data.stream()
                .filter(a -> a.personId().equals(personId) && a.covers(date))
                .findFirst();
    }

    @Override
    public List<Assignment> findByPerson(String personId) {
        return data.stream().filter(a -> a.personId().equals(personId)).toList();
    }

    @Override
    public void save(Assignment assignment) {
        data.add(assignment);
    }
}
