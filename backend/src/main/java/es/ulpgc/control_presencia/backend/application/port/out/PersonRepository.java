package es.ulpgc.control_presencia.backend.application.port.out;

import es.ulpgc.control_presencia.backend.domain.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository {
    Optional<Person> findById(String id);
    List<Person> findAll();
    void save(Person person);
}
