package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.GetPresenceStatusUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.AssignmentRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ClockEventRepository;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.application.port.out.TimeProvider;
import es.ulpgc.control_presencia.backend.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GetPresenceStatusService implements GetPresenceStatusUseCase {
    /** Hours around the scheduled slots in which clock events still belong to that shift. */
    static final long WINDOW_MARGIN_HOURS = 4;

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

        List<TimeSlot> slots = schedule.slotsFor(date);
        LocalDateTime from;
        LocalDateTime to;
        if (slots.isEmpty()) {
            from = date.atStartOfDay();
            to = date.plusDays(1).atStartOfDay();
        } else {
            from = slots.get(0).startOn(date).minusHours(WINDOW_MARGIN_HOURS);
            to = slots.get(slots.size() - 1).endOn(date).plusHours(WINDOW_MARGIN_HOURS);
        }
        var shiftEvents = events.findBetween(personId, from, to);
        return evaluator.evaluate(schedule, date, shiftEvents, time.now());
    }
}