package es.ulpgc.control_presencia.backend.domain;

public class PersonNotFoundException extends DomainException {
    public PersonNotFoundException(String personId) { super("Person not found: " + personId); }
}
