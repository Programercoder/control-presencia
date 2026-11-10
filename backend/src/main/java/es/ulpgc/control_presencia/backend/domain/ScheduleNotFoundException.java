package es.ulpgc.control_presencia.backend.domain;

public class ScheduleNotFoundException extends DomainException {
    public ScheduleNotFoundException(String scheduleId) { super("Schedule not found: " + scheduleId); }
}
