package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.domain.Person;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class JpaPersonRepository implements PersonRepository {
    private final SpringDataPersonRepository jpa;

    public JpaPersonRepository(SpringDataPersonRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Person> findById(String id) {
        return jpa.findById(id).map(e -> new Person(e.getId(), e.getName()));
    }

    @Override
    public List<Person> findAll() {
        return jpa.findAll().stream()
                .sorted(Comparator.comparing(PersonEntity::getName))
                .map(e -> new Person(e.getId(), e.getName()))
                .toList();
    }

    @Override
    public void save(Person person) {
        jpa.save(new PersonEntity(person.id(), person.name()));
    }
}
