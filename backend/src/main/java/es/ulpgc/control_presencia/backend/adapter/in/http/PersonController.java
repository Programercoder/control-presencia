package es.ulpgc.control_presencia.backend.adapter.in.http;

import es.ulpgc.control_presencia.backend.adapter.in.http.ApiDtos.PersonResponse;
import es.ulpgc.control_presencia.backend.application.port.in.ListPersonsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PersonController {
    private final ListPersonsUseCase persons;

    public PersonController(ListPersonsUseCase persons) {
        this.persons = persons;
    }

    @GetMapping("/people")
    public List<PersonResponse> people() {
        return persons.list().stream().map(p -> new PersonResponse(p.id(), p.name())).toList();
    }
}
