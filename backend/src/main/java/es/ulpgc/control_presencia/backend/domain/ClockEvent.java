package es.ulpgc.control_presencia.backend.domain;

import java.time.LocalDateTime;

public record ClockEvent(String personId, LocalDateTime timestamp, ClockEventType type, EventSource source) {
    public ClockEvent {
        if (personId == null || personId.isBlank()) throw new DomainException("Missing person");
        if (timestamp == null) throw new DomainException("Missing date and time");
        if (type == null) throw new DomainException("Missing event type");
        if (source == null) throw new DomainException("Missing event source");
    }
}
