package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.RegisterClockEventUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.AssignmentRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ClockEventRepository;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.application.port.out.TimeProvider;
import es.ulpgc.control_presencia.backend.domain.*;

public class RegisterClockEventService implements RegisterClockEventUseCase {
    private final PersonRepository persons;
    private final AssignmentRepository assignments;
    private final ScheduleRepository schedules;
    private final ClockEventRepository events;
    private final TimeProvider time;
    private final ClockEventValidator validator = new ClockEventValidator();

    public RegisterClockEventService(PersonRepository persons, AssignmentRepository assignments,
                                     ScheduleRepository schedules, ClockEventRepository events,
                                     TimeProvider time) {
        this.persons = persons;
        this.assignments = assignments;
        this.schedules = schedules;
        this.events = events;
        this.time = time;
    }

    @Override
    public ClockEvent register(String personId, ClockEventType type, EventSource source) {
        persons.findById(personId).orElseThrow(() -> new PersonNotFoundException(personId));
        var now = time.now();
        var today = now.toLocalDate();
        var assignment = assignments.findActive(personId, today)
                .orElseThrow(() -> new ScheduleNotAssignedException("No schedule assigned on " + today));

        if (type == ClockEventType.CLOCK_IN) {
            Schedule schedule = schedules.findById(assignment.scheduleId())
                    .orElseThrow(() -> new ScheduleNotAssignedException(
                            "Schedule not found: " + assignment.scheduleId()));
            if (schedule.slotsFor(today).isEmpty()) {
                throw new NoShiftTodayException("No shift scheduled for " + personId + " on " + today);
            }
        }

        var event = new ClockEvent(personId, now, type, source);
        validator.validate(events.findLast(personId), event);
        events.save(event);
        return event;
    }
}
