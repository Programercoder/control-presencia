package es.ulpgc.control_presencia.backend.domain;

import java.util.List;

public record PresenceResult(
        PresenceStatus status,
        long expectedMinutes,
        long workedMinutes,
        long deviationMinutes,
        List<Incident> incidents) { }
