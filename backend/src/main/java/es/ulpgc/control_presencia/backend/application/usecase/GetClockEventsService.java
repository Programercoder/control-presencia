package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.GetClockEventsUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.ClockEventRepository;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.domain.ClockEvent;
import es.ulpgc.control_presencia.backend.domain.DomainException;
import es.ulpgc.control_presencia.backend.domain.PersonNotFoundException;

import java.time.LocalDate;
import java.util.List;

public class GetClockEventsService implements GetClockEventsUseCase {
    private final PersonRepository persons;
    private final ClockEventRepository events;

    public GetClockEventsService(PersonRepository persons, ClockEventRepository events) {
        this.persons = persons;
        this.events = events;
    }

    @Override
    public List<ClockEvent> getEvents(String personId, LocalDate from, LocalDate to) {
        persons.findById(personId).orElseThrow(() -> new PersonNotFoundException(personId));
        if (from == null || to == null) throw new DomainException("Missing date range");
        if (to.isBefore(from)) throw new DomainException("End date is before start date");
        return events.findBetween(personId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }
}
