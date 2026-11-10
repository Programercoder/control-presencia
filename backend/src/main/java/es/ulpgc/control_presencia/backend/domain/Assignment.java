package es.ulpgc.control_presencia.backend.domain;

import java.time.LocalDate;

public record Assignment(String personId, String scheduleId, LocalDate from, LocalDate to) {
    public Assignment {
        if (personId == null || personId.isBlank()) throw new DomainException("Missing person");
        if (scheduleId == null || scheduleId.isBlank()) throw new DomainException("Missing schedule");
        if (from == null) throw new DomainException("Missing start date");
        if (to != null && to.isBefore(from)) throw new DomainException("End date is before start date");
    }

    /** `to` null = open-ended assignment. */
    public boolean covers(LocalDate date) {
        return !date.isBefore(from) && (to == null || !date.isAfter(to));
    }

    public boolean overlaps(Assignment other) {
        boolean thisStartsBeforeOtherEnds = other.to == null || !from.isAfter(other.to);
        boolean otherStartsBeforeThisEnds = to == null || !other.from.isAfter(to);
        return thisStartsBeforeOtherEnds && otherStartsBeforeThisEnds;
    }
}