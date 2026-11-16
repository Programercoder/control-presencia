package es.ulpgc.control_presencia.backend.domain;

/** `justification` is null when the incident has not been justified. */
public record Incident(IncidentType type, long minutes, String description, String justification) {

    public Incident(IncidentType type, long minutes, String description) {
        this(type, minutes, description, null);
    }

    public boolean isJustified() {
        return justification != null;
    }

    public Incident justifiedWith(String reason) {
        return new Incident(type, minutes, description, reason);
    }
}