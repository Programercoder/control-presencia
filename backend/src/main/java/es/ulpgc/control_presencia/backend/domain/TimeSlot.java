package es.ulpgc.control_presencia.backend.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Time range. If end <= start, it crosses midnight (e.g. 20:00-08:00). */
public record TimeSlot(LocalTime start, LocalTime end) {
    public TimeSlot {
        if (start == null || end == null) throw new DomainException("A time slot needs start and end");
        if (start.equals(end)) throw new DomainException("A time slot cannot last 0 hours");
    }

    public boolean crossesMidnight() { return end.isBefore(start); }

    public Duration duration() {
        Duration d = Duration.between(start, end);
        return d.isNegative() ? d.plusHours(24) : d;
    }

    public LocalDateTime startOn(LocalDate date) { return date.atTime(start); }

    public LocalDateTime endOn(LocalDate date) {
        return crossesMidnight() ? date.plusDays(1).atTime(end) : date.atTime(end);
    }
}