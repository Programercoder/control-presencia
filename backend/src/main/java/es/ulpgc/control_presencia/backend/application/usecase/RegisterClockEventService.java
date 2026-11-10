package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.RegisterClockEventUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.AssignmentRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ClockEventRepository;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.application.port.out.TimeProvider;
import es.ulpgc.control_presencia.backend.domain.*;

public class RegisterClockEventService implements RegisterClockEventUseCase {
    private final PersonRepository persons;
    private final AssignmentRepository assignments;
    private final ClockEventRepository events;
    private final TimeProvider time;
    private final ClockEventValidator validator = new ClockEventValidator();

    public RegisterClockEventService(PersonRepository persons, AssignmentRepository assignments,
                                     ClockEventRepository events, TimeProvider time) {
        this.persons = persons;
        this.assignments = assignments;
        this.events = events;
        this.time = time;
    }

    @Override
    public ClockEvent register(String personId, ClockEventType type, EventSource source) {
        persons.findById(personId).orElseThrow(() -> new PersonNotFoundException(personId));
        var now = time.now();
        assignments.findActive(personId, now.toLocalDate())
                .orElseThrow(() -> new ScheduleNotAssignedException("No schedule assigned on " + now.toLocalDate()));
        var event = new ClockEvent(personId, now, type, source);
        validator.validate(events.findLast(personId), event);
        events.save(event);
        return event;
    }
}
