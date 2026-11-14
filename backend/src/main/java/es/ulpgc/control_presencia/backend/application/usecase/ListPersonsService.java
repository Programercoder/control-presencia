package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.ListPersonsUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.domain.Person;

import java.util.List;

public class ListPersonsService implements ListPersonsUseCase {
    private final PersonRepository persons;

    public ListPersonsService(PersonRepository persons) {
        this.persons = persons;
    }

    @Override
    public List<Person> list() {
        return persons.findAll();
    }
}
