package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.AssignScheduleUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.AssignmentRepository;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.domain.*;

import java.time.LocalDate;

public class AssignScheduleService implements AssignScheduleUseCase {
    private final PersonRepository persons;
    private final ScheduleRepository schedules;
    private final AssignmentRepository assignments;

    public AssignScheduleService(PersonRepository persons, ScheduleRepository schedules,
                                 AssignmentRepository assignments) {
        this.persons = persons;
        this.schedules = schedules;
        this.assignments = assignments;
    }

    @Override
    public Assignment assign(String personId, String scheduleId, LocalDate from, LocalDate to) {
        persons.findById(personId).orElseThrow(() -> new PersonNotFoundException(personId));
        schedules.findById(scheduleId).orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        var assignment = new Assignment(personId, scheduleId, from, to);
        boolean overlaps = assignments.findByPerson(personId).stream().anyMatch(assignment::overlaps);
        if (overlaps) {
            throw new AssignmentOverlapException("The person already has a schedule in that period");
        }
        assignments.save(assignment);
        return assignment;
    }
}
