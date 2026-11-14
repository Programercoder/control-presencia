package es.ulpgc.control_presencia.backend.adapter.in.http;

import es.ulpgc.control_presencia.backend.adapter.in.http.ApiDtos.*;
import es.ulpgc.control_presencia.backend.application.port.in.ScheduleDefinition;
import es.ulpgc.control_presencia.backend.application.port.in.ScheduleType;
import es.ulpgc.control_presencia.backend.domain.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

final class HttpMapper {
    private HttpMapper() { }

    // ---------- validation helpers (missing or invalid data -> DomainException -> 422) ----------

    static String require(String value, String field) {
        if (value == null || value.isBlank()) throw new DomainException("Missing field: " + field);
        return value.trim();
    }

    static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        String text = require(value, field);
        try {
            return Enum.valueOf(type, text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid value for " + field + ": " + text);
        }
    }

    static LocalTime parseTime(String value, String field) {
        try {
            return LocalTime.parse(require(value, field));
        } catch (DateTimeParseException e) {
            throw new DomainException("Invalid time for " + field + ": " + value + " (use HH:mm)");
        }
    }

    // ---------- clock events and presence ----------

    static ClockEventResponse toResponse(ClockEvent e) {
        return new ClockEventResponse(e.personId(), e.timestamp().toString(), e.type().name(), e.source().name());
    }

    static PresenceResponse toResponse(String personId, LocalDate date, PresenceResult r) {
        List<IncidentResponse> incidents = r.incidents().stream()
                .map(i -> new IncidentResponse(i.type().name(), i.minutes(), i.description()))
                .toList();
        return new PresenceResponse(personId, date.toString(), r.status().name(),
                r.expectedMinutes(), r.workedMinutes(), r.deviationMinutes(), incidents);
    }

    // ---------- schedules ----------

    static ScheduleDefinition toDefinition(ScheduleRequest r) {
        ScheduleType type = parseEnum(ScheduleType.class, r.type(), "type");
        Set<DayOfWeek> days = r.days() == null ? Set.of()
                : r.days().stream().map(d -> parseEnum(DayOfWeek.class, d, "days")).collect(java.util.stream.Collectors.toSet());
        return new ScheduleDefinition(type, r.name(), days, toSlots(r.slots()), r.cycleStart(), toShifts(r.cycle()));
    }

    private static List<TimeSlot> toSlots(List<SlotDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(s -> new TimeSlot(parseTime(s.start(), "slot.start"), parseTime(s.end(), "slot.end")))
                .toList();
    }

    private static List<Shift> toShifts(List<ShiftDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(s -> new Shift(s.name(), toSlots(s.slots()))).toList();
    }

    static ScheduleResponse toResponse(String id, Schedule schedule) {
        if (schedule instanceof ContinuousSchedule c) {
            return new ScheduleResponse(id, "CONTINUOUS", c.name(), dayNames(c.days()),
                    List.of(toDto(c.slot())), null, null);
        }
        if (schedule instanceof SplitSchedule s) {
            return new ScheduleResponse(id, "SPLIT", s.name(), dayNames(s.days()),
                    s.slots().stream().map(HttpMapper::toDto).toList(), null, null);
        }
        if (schedule instanceof ShiftSchedule t) {
            List<ShiftDto> cycle = t.cycle().stream()
                    .map(sh -> new ShiftDto(sh.name(), sh.slots().stream().map(HttpMapper::toDto).toList()))
                    .toList();
            return new ScheduleResponse(id, "SHIFTS", t.name(), List.of(), List.of(), t.cycleStart(), cycle);
        }
        throw new IllegalArgumentException("Unsupported schedule: " + schedule.getClass());
    }

    private static List<String> dayNames(Set<DayOfWeek> days) {
        return days.stream().sorted(Comparator.naturalOrder()).map(Enum::name).toList();
    }

    private static SlotDto toDto(TimeSlot slot) {
        return new SlotDto(slot.start().toString(), slot.end().toString());
    }
}
