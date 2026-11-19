package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import es.ulpgc.control_presencia.backend.application.port.out.AssignmentRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ClockEventRepository;
import es.ulpgc.control_presencia.backend.application.port.out.JustificationRepository;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PersistenceIntegrationTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 9);

    @Autowired PersonRepository persons;
    @Autowired ScheduleRepository schedules;
    @Autowired AssignmentRepository assignments;
    @Autowired ClockEventRepository events;
    @Autowired JustificationRepository justifications;

    @Test
    void schedulesSurviveRoundTrip() {
        schedules.save("t-office", new ContinuousSchedule("Office", Set.of(DayOfWeek.MONDAY),
                new TimeSlot(LocalTime.of(8, 0), LocalTime.of(15, 0))));
        schedules.save("t-restaurant", new SplitSchedule("Restaurant", Set.of(DayOfWeek.MONDAY),
                List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)),
                        new TimeSlot(LocalTime.of(17, 0), LocalTime.of(21, 0)))));
        schedules.save("t-hospital", new ShiftSchedule("Hospital", MONDAY,
                List.of(Shift.of("ON_CALL", new TimeSlot(LocalTime.of(20, 0), LocalTime.of(8, 0))), Shift.off())));

        assertEquals(1, schedules.findById("t-office").orElseThrow().slotsFor(MONDAY).size());
        assertEquals(2, schedules.findById("t-restaurant").orElseThrow().slotsFor(MONDAY).size());
        var hospital = schedules.findById("t-hospital").orElseThrow();
        assertInstanceOf(ShiftSchedule.class, hospital);
        assertTrue(hospital.slotsFor(MONDAY).get(0).crossesMidnight());
        assertTrue(hospital.slotsFor(MONDAY.plusDays(1)).isEmpty());
    }

    @Test
    void personAndAssignmentArePersisted() {
        persons.save(new Person("t-p1", "Test Person"));
        assignments.save(new Assignment("t-p1", "t-office", MONDAY, null));

        assertEquals("Test Person", persons.findById("t-p1").orElseThrow().name());
        assertTrue(assignments.findActive("t-p1", MONDAY.plusDays(5)).isPresent());
        assertTrue(assignments.findActive("t-p1", MONDAY.minusDays(1)).isEmpty());
    }

    @Test
    void clockEventsAreStoredInOrder() {
        events.save(new ClockEvent("t-p2", MONDAY.atTime(8, 0), ClockEventType.CLOCK_IN, EventSource.WEB));
        events.save(new ClockEvent("t-p2", MONDAY.atTime(15, 0), ClockEventType.CLOCK_OUT, EventSource.TERMINAL));

        assertEquals(ClockEventType.CLOCK_OUT, events.findLast("t-p2").orElseThrow().type());
        var day = events.findBetween("t-p2", MONDAY.atStartOfDay(), MONDAY.plusDays(1).atStartOfDay());
        assertEquals(2, day.size());
        assertEquals(ClockEventType.CLOCK_IN, day.get(0).type());
        assertTrue(events.findBetween("t-p2", MONDAY.plusDays(1).atStartOfDay(),
                MONDAY.plusDays(2).atStartOfDay()).isEmpty());
    }

    @Test
    void justificationsArePersisted() {
        justifications.save(new Justification("t-p3", MONDAY, IncidentType.LATE_ARRIVAL,
                "Bus strike", "HR", LocalDateTime.of(2026, 11, 9, 17, 0)));

        var found = justifications.findByPersonAndDate("t-p3", MONDAY);
        assertEquals(1, found.size());
        assertEquals("Bus strike", found.get(0).reason());
        assertEquals(1, justifications.findBetween("t-p3", MONDAY, MONDAY.plusDays(1)).size());
    }
}
