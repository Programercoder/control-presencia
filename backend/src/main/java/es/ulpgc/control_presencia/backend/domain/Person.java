package es.ulpgc.control_presencia.backend.domain;

public record Person(String id, String name) {
    public Person {
        if (id == null || id.isBlank()) throw new DomainException("A person needs an id");
        if (name == null || name.isBlank()) throw new DomainException("A person needs a name");
    }
}
