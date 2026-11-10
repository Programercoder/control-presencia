package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.domain.Person;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryPersonRepository implements PersonRepository {
    private final Map<String, Person> data = new HashMap<>();

    @Override
    public Optional<Person> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public void save(Person person) {
        data.put(person.id(), person);
    }
}
