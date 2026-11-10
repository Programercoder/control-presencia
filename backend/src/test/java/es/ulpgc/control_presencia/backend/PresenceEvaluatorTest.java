package es.ulpgc.control_presencia.backend;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PresenceEvaluatorTest {
    private final PresenceEvaluator evaluator = new PresenceEvaluator(5);
    private final LocalDate monday = LocalDate.of(2026, 11, 9);

    private ClockEvent event(LocalDateTime t, ClockEventType type) {
        return new ClockEvent("p1", t, type, EventSource.WEB);
    }

    @Test
    void continuousScheduleWithLateArrival() {
        Schedule schedule = new ContinuousSchedule("Office", Set.of(DayOfWeek.MONDAY),
                new TimeSlot(LocalTime.of(8, 0), LocalTime.of(15, 0)));
        var result = evaluator.evaluate(schedule, monday,
                List.of(event(monday.atTime(8, 10), ClockEventType.CLOCK_IN),
                        event(monday.atTime(15, 0), ClockEventType.CLOCK_OUT)),
                monday.atTime(16, 0));
        assertEquals(PresenceStatus.WITH_INCIDENT, result.status());
        assertEquals(IncidentType.LATE_ARRIVAL, result.incidents().get(0).type());
    }

    @Test
    void splitScheduleSumsBothSlots() {
        Schedule schedule = new SplitSchedule("Restaurant", Set.of(DayOfWeek.MONDAY),
                List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)),
                        new TimeSlot(LocalTime.of(17, 0), LocalTime.of(21, 0))));
        var result = evaluator.evaluate(schedule, monday,
                List.of(event(monday.atTime(9, 0), ClockEventType.CLOCK_IN),
                        event(monday.atTime(13, 0), ClockEventType.CLOCK_OUT),
                        event(monday.atTime(17, 0), ClockEventType.CLOCK_IN),
                        event(monday.atTime(21, 0), ClockEventType.CLOCK_OUT)),
                monday.atTime(22, 0));
        assertEquals(480, result.expectedMinutes());
        assertEquals(480, result.workedMinutes());
    }

    @Test
    void overnightOnCallCrossesMidnight() {
        Schedule schedule = new ShiftSchedule("Hospital", monday,
                List.of(Shift.of("ON_CALL", new TimeSlot(LocalTime.of(20, 0), LocalTime.of(8, 0))), Shift.off()));
        var result = evaluator.evaluate(schedule, monday,
                List.of(event(monday.atTime(20, 0), ClockEventType.CLOCK_IN),
                        event(monday.plusDays(1).atTime(8, 0), ClockEventType.CLOCK_OUT)),
                monday.plusDays(1).atTime(9, 0));
        assertEquals(PresenceStatus.COMPLETED, result.status());
        assertEquals(720, result.workedMinutes());
    }

    @Test
    void clockOutWithoutClockInIsInconsistent() {
        var validator = new ClockEventValidator();
        assertThrows(InconsistentClockEventException.class, () ->
                validator.validate(Optional.empty(), event(monday.atTime(15, 0), ClockEventType.CLOCK_OUT)));
    }
}
