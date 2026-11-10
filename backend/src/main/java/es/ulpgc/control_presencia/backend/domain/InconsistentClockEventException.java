package es.ulpgc.control_presencia.backend.domain;

public class InconsistentClockEventException extends DomainException {
    public InconsistentClockEventException(String message) { super(message); }
}
