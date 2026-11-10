package es.ulpgc.control_presencia.backend.domain;

public class DuplicateClockEventException extends DomainException {
    public DuplicateClockEventException(String message) { super(message); }
}
