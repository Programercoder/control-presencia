package es.ulpgc.control_presencia.backend.domain;

public class DuplicateScheduleException extends DomainException {
    public DuplicateScheduleException(String scheduleId) { super("Schedule already exists: " + scheduleId); }
}
