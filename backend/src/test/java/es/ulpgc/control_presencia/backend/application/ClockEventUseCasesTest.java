package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.usecase.GetPresenceStatusService;
import es.ulpgc.control_presencia.backend.application.usecase.RegisterClockEventService;
import es.ulpgc.control_presencia.backend.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClockEventUseCasesTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 9);

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
        persons.save(new Person("p2", "Person Without Schedule"));
        schedules.save("office", new ContinuousSchedule("Office", Set.of(DayOfWeek.MONDAY),
                new TimeSlot(LocalTime.of(8, 0), LocalTime.of(15, 0))));
        assignments.save(new Assignment("p1", "office", LocalDate.of(2026, 11, 1), null));

        register = new RegisterClockEventService(persons, assignments, events, time);
        status = new GetPresenceStatusService(persons, assignments, schedules, events, time,
                new PresenceEvaluator(5));
    }

    @Test
    void registersClockIn() {
        ClockEvent event = register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB);
        assertEquals(ClockEventType.CLOCK_IN, event.type());
        assertEquals(MONDAY.atTime(8, 0), event.timestamp());
    }

    @Test
    void unknownPersonIsRejected() {
        assertThrows(PersonNotFoundException.class,
                () -> register.register("nobody", ClockEventType.CLOCK_IN, EventSource.WEB));
    }

    @Test
    void personWithoutScheduleIsRejected() {
        assertThrows(ScheduleNotAssignedException.class,
                () -> register.register("p2", ClockEventType.CLOCK_IN, EventSource.WEB));
    }

    @Test
    void clockOutWithoutClockInIsRejected() {
        assertThrows(InconsistentClockEventException.class,
                () -> register.register("p1", ClockEventType.CLOCK_OUT, EventSource.WEB));
    }

    @Test
    void duplicateClockInIsRejected() {
        register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB);
        assertThrows(DuplicateClockEventException.class,
                () -> register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB));
    }

    @Test
    void secondClockInWithoutClockOutIsRejected() {
        register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB);
        time.set(MONDAY.atTime(9, 0));
        assertThrows(InconsistentClockEventException.class,
                () -> register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB));
    }

    @Test
    void statusAfterLateArrivalShowsIncident() {
        time.set(MONDAY.atTime(8, 10));
        register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB);
        time.set(MONDAY.atTime(15, 0));
        register.register("p1", ClockEventType.CLOCK_OUT, EventSource.WEB);
        time.set(MONDAY.atTime(16, 0));

        PresenceResult result = status.getStatus("p1", MONDAY);

        assertEquals(PresenceStatus.WITH_INCIDENT, result.status());
        assertEquals(IncidentType.LATE_ARRIVAL, result.incidents().get(0).type());
        assertEquals(410, result.workedMinutes());
    }

    @Test
    void statusWithoutAssignmentIsRejected() {
        assertThrows(ScheduleNotAssignedException.class, () -> status.getStatus("p2", MONDAY));
    }
}
