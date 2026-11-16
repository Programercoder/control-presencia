package es.ulpgc.control_presencia.backend.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;


public record Justification(String personId, LocalDate date, IncidentType incidentType,
                            String reason, String author, LocalDateTime recordedAt) {
    public static final int MAX_REASON_LENGTH = 300;

    public Justification {
        if (personId == null || personId.isBlank()) throw new DomainException("Missing person");
        if (date == null) throw new DomainException("Missing date");
        if (incidentType == null) throw new DomainException("Missing incident type");
        if (reason == null || reason.isBlank()) throw new DomainException("A justification needs a reason");
        if (reason.length() > MAX_REASON_LENGTH) {
            throw new DomainException("The reason is too long (max " + MAX_REASON_LENGTH + " characters)");
        }
        if (author == null || author.isBlank()) throw new DomainException("A justification needs an author");
        if (recordedAt == null) throw new DomainException("Missing recording time");
        reason = reason.trim();
        author = author.trim();
    }
}
