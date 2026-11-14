package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.usecase.GetPresenceStatusService;
import es.ulpgc.control_presencia.backend.application.usecase.RegisterClockEventService;
import es.ulpgc.control_presencia.backend.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventWindowTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 9);
    private static final LocalDate TUESDAY = MONDAY.plusDays(1);

    private InMemoryPersonRepository persons;
    private InMemoryScheduleRepository schedules;
    private InMemoryAssignmentRepository assignments;
    private InMemoryClockEventRepository events;
    private FixedTimeProvider time;
    private RegisterClockEventService register;
    private GetPresenceStatusService status;

    @BeforeEach
    void setUp() {
        persons = new InMemoryPersonRepository();
        schedules = new InMemoryScheduleRepository();
        assignments = new InMemoryAssignmentRepository();
        events = new InMemoryClockEventRepository();
        time = new FixedTimeProvider(MONDAY.atTime(8, 0));
        persons.save(new Person("p1", "Test Person"));
        persons.save(new Person("p2", "Night Person"));
        schedules.save("office", new ContinuousSchedule("Office", Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                new TimeSlot(LocalTime.of(8, 0), LocalTime.of(15, 0))));
        schedules.save("hospital", new ShiftSchedule("Hospital", MONDAY,
                List.of(Shift.of("ON_CALL", new TimeSlot(LocalTime.of(20, 0), LocalTime.of(8, 0))), Shift.off())));
        assignments.save(new Assignment("p1", "office", MONDAY, null));
        assignments.save(new Assignment("p2", "hospital", MONDAY, null));
        register = new RegisterClockEventService(persons, assignments, events, time);
        status = new GetPresenceStatusService(persons, assignments, schedules, events, time,
                new PresenceEvaluator(5));
    }

    @Test
    void nextDayClockInDoesNotLeakIntoPreviousDay() {
        register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB);
        time.set(MONDAY.atTime(15, 0));
        register.register("p1", ClockEventType.CLOCK_OUT, EventSource.WEB);
        time.set(TUESDAY.atTime(8, 0));
        register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB);
        time.set(TUESDAY.atTime(9, 0));

        PresenceResult monday = status.getStatus("p1", MONDAY);

        assertEquals(PresenceStatus.COMPLETED, monday.status());
        assertEquals(420, monday.workedMinutes());
    }

    @Test
    void overnightOnCallStillCapturesNextDayClockOut() {
        time.set(MONDAY.atTime(20, 0));
        register.register("p2", ClockEventType.CLOCK_IN, EventSource.TERMINAL);
        time.set(TUESDAY.atTime(8, 0));
        register.register("p2", ClockEventType.CLOCK_OUT, EventSource.TERMINAL);
        time.set(TUESDAY.atTime(9, 0));

        PresenceResult result = status.getStatus("p2", MONDAY);

        assertEquals(PresenceStatus.COMPLETED, result.status());
        assertEquals(720, result.workedMinutes());
    }
}
