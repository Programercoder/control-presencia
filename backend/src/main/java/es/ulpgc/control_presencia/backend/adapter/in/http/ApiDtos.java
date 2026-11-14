package es.ulpgc.control_presencia.backend.adapter.in.http;

import java.time.LocalDate;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() { }

    public record ClockEventRequest(String personId, String type, String source) { }

    public record ClockEventResponse(String personId, String timestamp, String type, String source) { }

    public record IncidentResponse(String type, long minutes, String description) { }

    public record PresenceResponse(String personId, String date, String status,
                                   long expectedMinutes, long workedMinutes, long deviationMinutes,
                                   List<IncidentResponse> incidents) { }

    public record SlotDto(String start, String end) { }

    public record ShiftDto(String name, List<SlotDto> slots) { }

    public record ScheduleRequest(String id, String type, String name, List<String> days,
                                  List<SlotDto> slots, LocalDate cycleStart, List<ShiftDto> cycle) { }

    public record ScheduleResponse(String id, String type, String name, List<String> days,
                                   List<SlotDto> slots, LocalDate cycleStart, List<ShiftDto> cycle) { }

    public record AssignmentRequest(String personId, String scheduleId, LocalDate from, LocalDate to) { }

    public record AssignmentResponse(String personId, String scheduleId, LocalDate from, LocalDate to) { }

    public record PersonResponse(String id, String name) { }
}
