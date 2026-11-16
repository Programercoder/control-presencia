package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.ListJustificationsUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.JustificationRepository;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.domain.DomainException;
import es.ulpgc.control_presencia.backend.domain.Justification;
import es.ulpgc.control_presencia.backend.domain.PersonNotFoundException;

import java.time.LocalDate;
import java.util.List;

public class ListJustificationsService implements ListJustificationsUseCase {
    private final PersonRepository persons;
    private final JustificationRepository justifications;

    public ListJustificationsService(PersonRepository persons, JustificationRepository justifications) {
        this.persons = persons;
        this.justifications = justifications;
    }

    @Override
    public List<Justification> list(String personId, LocalDate from, LocalDate to) {
        persons.findById(personId).orElseThrow(() -> new PersonNotFoundException(personId));
        if (from == null || to == null) throw new DomainException("Missing date range");
        if (to.isBefore(from)) throw new DomainException("End date is before start date");
        return justifications.findBetween(personId, from, to);
    }
}
