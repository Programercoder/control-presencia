package es.ulpgc.control_presencia.backend.application.port.in;

import es.ulpgc.control_presencia.backend.domain.Person;

import java.util.List;

public interface ListPersonsUseCase {
    List<Person> list();
}
