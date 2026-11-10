package es.ulpgc.control_presencia.backend.application.port.out;

import es.ulpgc.control_presencia.backend.domain.Person;
import java.util.Optional;

public interface PersonRepository {
    Optional<Person> findById(String id);
    void save(Person person);
}
