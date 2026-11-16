package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.usecase.*;
import es.ulpgc.control_presencia.backend.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JustificationUseCasesTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 9);

    private InMemoryJustificationRepository justifications;
    private FixedTimeProvider time;
    private RegisterClockEventService register;
    private JustifiedPresenceService presence;
    private JustifyIncidentService justify;

    @BeforeEach
    void setUp() {
        var persons = new InMemoryPersonRepository();
        var schedules = new InMemoryScheduleRepository();
        var assignments = new InMemoryAssignmentRepository();
        var events = new InMemoryClockEventRepository();
        justifications = new InMemoryJustificationRepository();
        time = new FixedTimeProvider(MONDAY.atTime(8, 0));

        persons.save(new Person("p1", "Test Person"));
        schedules.save("office", new ContinuousSchedule("Office", Set.of(DayOfWeek.MONDAY),
                new TimeSlot(LocalTime.of(8, 0), LocalTime.of(15, 0))));
        assignments.save(new Assignment("p1", "office", MONDAY, null));

        register = new RegisterClockEventService(persons, assignments, schedules, events, time);
        var base = new GetPresenceStatusService(persons, assignments, schedules, events, time,
                new PresenceEvaluator(5));
        presence = new JustifiedPresenceService(base, justifications);
        justify = new JustifyIncidentService(presence, justifications, time);
    }

    private void lateDay() {
        time.set(MONDAY.atTime(8, 10));
        register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB);
        time.set(MONDAY.atTime(15, 0));
        register.register("p1", ClockEventType.CLOCK_OUT, EventSource.WEB);
        time.set(MONDAY.atTime(16, 0));
    }

    @Test
    void justifiedIncidentAppearsInPresenceStatus() {
        lateDay();
        justify.justify("p1", MONDAY, IncidentType.LATE_ARRIVAL, "Bus strike", "HR");

        Incident incident = presence.getStatus("p1", MONDAY).incidents().get(0);

        assertTrue(incident.isJustified());
        assertEquals("Bus strike", incident.justification());
        assertEquals(1, justifications.findByPersonAndDate("p1", MONDAY).size());
    }

    @Test
    void unjustifiedIncidentStaysUnjustified() {
        lateDay();
        assertFalse(presence.getStatus("p1", MONDAY).incidents().get(0).isJustified());
    }

    @Test
    void justifyingTwiceIsRejected() {
        lateDay();
        justify.justify("p1", MONDAY, IncidentType.LATE_ARRIVAL, "Bus strike", "HR");
        assertThrows(DuplicateJustificationException.class,
                () -> justify.justify("p1", MONDAY, IncidentType.LATE_ARRIVAL, "Other", "HR"));
    }

    @Test
    void justifyingAnIncidentThatDoesNotExistIsRejected() {
        lateDay();
        assertThrows(NoSuchIncidentException.class,
                () -> justify.justify("p1", MONDAY, IncidentType.ABSENCE, "Sick", "HR"));
    }

    @Test
    void blankReasonIsRejected() {
        lateDay();
        assertThrows(DomainException.class,
                () -> justify.justify("p1", MONDAY, IncidentType.LATE_ARRIVAL, "  ", "HR"));
    }

    @Test
    void unknownPersonIsRejected() {
        assertThrows(PersonNotFoundException.class,
                () -> justify.justify("nobody", MONDAY, IncidentType.LATE_ARRIVAL, "x", "HR"));
    }
}
