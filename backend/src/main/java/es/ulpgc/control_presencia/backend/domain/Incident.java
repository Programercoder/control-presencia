package es.ulpgc.control_presencia.backend.domain;

public record Incident(IncidentType type, long minutes, String description) { }
