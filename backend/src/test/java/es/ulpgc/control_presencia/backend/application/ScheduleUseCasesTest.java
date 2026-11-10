package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.port.in.ScheduleDefinition;
import es.ulpgc.control_presencia.backend.application.port.in.ScheduleType;
import es.ulpgc.control_presencia.backend.application.usecase.*;
import es.ulpgc.control_presencia.backend.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleUseCasesTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 9);

    private InMemoryPersonRepository persons;
    private InMemoryScheduleRepository schedules;
    private InMemoryAssignmentRepository assignments;
    private InMemoryClockEventRepository events;
    private FixedTimeProvider time;
    private DefineScheduleService define;
    private AssignScheduleService assign;

    @BeforeEach
    void setUp() {
        persons = new InMemoryPersonRepository();
        schedules = new InMemoryScheduleRepository();
        assignments = new InMemoryAssignmentRepository();
        events = new InMemoryClockEventRepository();
        time = new FixedTimeProvider(MONDAY.atTime(8, 0));
        persons.save(new Person("p1", "Test Person"));
        define = new DefineScheduleService(schedules);
        assign = new AssignScheduleService(persons, schedules, assignments);
    }

    private ScheduleDefinition office() {
        return new ScheduleDefinition(ScheduleType.CONTINUOUS, "Office",
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                List.of(new TimeSlot(LocalTime.of(8, 0), LocalTime.of(15, 0))), null, null);
    }

    @Test
    void definesThreeScheduleTypes() {
        define.define("office", office());
        define.define("restaurant", new ScheduleDefinition(ScheduleType.SPLIT, "Restaurant",
                Set.of(DayOfWeek.MONDAY),
                List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)),
                        new TimeSlot(LocalTime.of(17, 0), LocalTime.of(21, 0))), null, null));
        define.define("hospital", new ScheduleDefinition(ScheduleType.SHIFTS, "Hospital",
                null, null, MONDAY,
                List.of(Shift.of("ON_CALL", new TimeSlot(LocalTime.of(20, 0), LocalTime.of(8, 0))), Shift.off())));

        assertInstanceOf(ContinuousSchedule.class, schedules.findById("office").orElseThrow());
        assertInstanceOf(SplitSchedule.class, schedules.findById("restaurant").orElseThrow());
        assertInstanceOf(ShiftSchedule.class, schedules.findById("hospital").orElseThrow());
    }

    @Test
    void duplicateScheduleIdIsRejected() {
        define.define("office", office());
        assertThrows(DuplicateScheduleException.class, () -> define.define("office", office()));
    }

    @Test
    void continuousScheduleWithTwoSlotsIsRejected() {
        var bad = new ScheduleDefinition(ScheduleType.CONTINUOUS, "Bad", Set.of(DayOfWeek.MONDAY),
                List.of(new TimeSlot(LocalTime.of(8, 0), LocalTime.of(10, 0)),
                        new TimeSlot(LocalTime.of(12, 0), LocalTime.of(14, 0))), null, null);
        assertThrows(DomainException.class, () -> define.define("bad", bad));
    }

    @Test
    void assignsScheduleToPerson() {
        define.define("office", office());
        assign.assign("p1", "office", MONDAY, null);
        assertTrue(assignments.findActive("p1", MONDAY.plusDays(30)).isPresent());
    }

    @Test
    void assignmentToUnknownPersonOrScheduleIsRejected() {
        define.define("office", office());
        assertThrows(PersonNotFoundException.class, () -> assign.assign("nobody", "office", MONDAY, null));
        assertThrows(ScheduleNotFoundException.class, () -> assign.assign("p1", "missing", MONDAY, null));
    }

    @Test
    void overlappingAssignmentIsRejected() {
        define.define("office", office());
        assign.assign("p1", "office", MONDAY, MONDAY.plusDays(30));
        assertThrows(AssignmentOverlapException.class,
                () -> assign.assign("p1", "office", MONDAY.plusDays(10), null));
        assertDoesNotThrow(() -> assign.assign("p1", "office", MONDAY.plusDays(31), null));
    }

    @Test
    void queriesEventsByPeriod() {
        define.define("office", office());
        assign.assign("p1", "office", MONDAY, null);
        var register = new RegisterClockEventService(persons, assignments, events, time);
        register.register("p1", ClockEventType.CLOCK_IN, EventSource.WEB);
        time.set(MONDAY.atTime(15, 0));
        register.register("p1", ClockEventType.CLOCK_OUT, EventSource.WEB);

        var query = new GetClockEventsService(persons, events);
        assertEquals(2, query.getEvents("p1", MONDAY, MONDAY).size());
        assertEquals(0, query.getEvents("p1", MONDAY.plusDays(1), MONDAY.plusDays(2)).size());
        assertThrows(DomainException.class, () -> query.getEvents("p1", MONDAY, MONDAY.minusDays(1)));
        assertThrows(PersonNotFoundException.class, () -> query.getEvents("nobody", MONDAY, MONDAY));
    }
}
