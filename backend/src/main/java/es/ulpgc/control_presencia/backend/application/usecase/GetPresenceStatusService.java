package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.GetPresenceStatusUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.AssignmentRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ClockEventRepository;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.application.port.out.TimeProvider;
import es.ulpgc.control_presencia.backend.domain.*;

import java.time.LocalDate;

public class GetPresenceStatusService implements GetPresenceStatusUseCase {
    private final PersonRepository persons;
    private final AssignmentRepository assignments;
    private final ScheduleRepository schedules;
    private final ClockEventRepository events;
    private final TimeProvider time;
    private final PresenceEvaluator evaluator;

    public GetPresenceStatusService(PersonRepository persons, AssignmentRepository assignments,
                                    ScheduleRepository schedules, ClockEventRepository events,
                                    TimeProvider time, PresenceEvaluator evaluator) {
        this.persons = persons;
        this.assignments = assignments;
        this.schedules = schedules;
        this.events = events;
        this.time = time;
        this.evaluator = evaluator;
    }

    @Override
    public PresenceResult getStatus(String personId, LocalDate date) {
        persons.findById(personId).orElseThrow(() -> new PersonNotFoundException(personId));
        var assignment = assignments.findActive(personId, date)
                .orElseThrow(() -> new ScheduleNotAssignedException("No schedule assigned on " + date));
        Schedule schedule = schedules.findById(assignment.scheduleId())
                .orElseThrow(() -> new ScheduleNotAssignedException("Schedule not found: " + assignment.scheduleId()));
        // Window includes the next day so overnight on-call clock-outs are captured
        var dayEvents = events.findBetween(personId, date.atStartOfDay(), date.plusDays(2).atStartOfDay());
        return evaluator.evaluate(schedule, date, dayEvents, time.now());
    }
}
