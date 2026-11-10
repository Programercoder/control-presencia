package es.ulpgc.control_presencia.backend.application.port.in;

import es.ulpgc.control_presencia.backend.domain.Shift;
import es.ulpgc.control_presencia.backend.domain.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;


public record ScheduleDefinition(
        ScheduleType type,
        String name,
        Set<DayOfWeek> days,
        List<TimeSlot> slots,
        LocalDate cycleStart,
        List<Shift> cycle) {

    public ScheduleDefinition {
        days = days == null ? Set.of() : Set.copyOf(days);
        slots = slots == null ? List.of() : List.copyOf(slots);
        cycle = cycle == null ? List.of() : List.copyOf(cycle);
    }
}
