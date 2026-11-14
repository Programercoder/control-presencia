package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import es.ulpgc.control_presencia.backend.application.port.in.ScheduleDefinition;
import es.ulpgc.control_presencia.backend.application.port.in.ScheduleType;
import es.ulpgc.control_presencia.backend.application.usecase.ScheduleFactory;
import es.ulpgc.control_presencia.backend.domain.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class ScheduleMapper {
    private ScheduleMapper() { }

    static ScheduleEntity toEntity(String id, Schedule schedule) {
        if (schedule instanceof ContinuousSchedule c) {
            return new ScheduleEntity(id, ScheduleType.CONTINUOUS.name(), c.name(),
                    formatDays(c.days()), formatSlots(List.of(c.slot())), null, null);
        }
        if (schedule instanceof SplitSchedule s) {
            return new ScheduleEntity(id, ScheduleType.SPLIT.name(), s.name(),
                    formatDays(s.days()), formatSlots(s.slots()), null, null);
        }
        if (schedule instanceof ShiftSchedule t) {
            return new ScheduleEntity(id, ScheduleType.SHIFTS.name(), t.name(),
                    null, null, t.cycleStart(), formatCycle(t.cycle()));
        }
        throw new IllegalArgumentException("Unsupported schedule: " + schedule.getClass());
    }

    static Schedule toDomain(ScheduleEntity e) {
        return ScheduleFactory.create(new ScheduleDefinition(
                ScheduleType.valueOf(e.getType()), e.getName(),
                parseDays(e.getDays()), parseSlots(e.getSlots()),
                e.getCycleStart(), parseCycle(e.getCycle())));
    }

    private static String formatDays(Set<DayOfWeek> days) {
        return EnumSet.copyOf(days).stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private static Set<DayOfWeek> parseDays(String text) {
        if (text == null || text.isBlank()) return Set.of();
        return Arrays.stream(text.split(",")).map(DayOfWeek::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
    }

    private static String formatSlots(List<TimeSlot> slots) {
        return slots.stream().map(s -> s.start() + "-" + s.end()).collect(Collectors.joining(";"));
    }

    private static List<TimeSlot> parseSlots(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<TimeSlot> result = new ArrayList<>();
        for (String part : text.split(";")) {
            String[] times = part.split("-");
            result.add(new TimeSlot(LocalTime.parse(times[0]), LocalTime.parse(times[1])));
        }
        return result;
    }

    private static String formatCycle(List<Shift> cycle) {
        return cycle.stream().map(s -> s.name() + "=" + formatSlots(s.slots()))
                .collect(Collectors.joining("#"));
    }

    private static List<Shift> parseCycle(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<Shift> result = new ArrayList<>();
        for (String part : text.split("#", -1)) {
            int eq = part.indexOf('=');
            result.add(new Shift(part.substring(0, eq), parseSlots(part.substring(eq + 1))));
        }
        return result;
    }
}
