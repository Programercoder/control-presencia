package es.ulpgc.control_presencia.backend.config;

import es.ulpgc.control_presencia.backend.application.port.in.AssignScheduleUseCase;
import es.ulpgc.control_presencia.backend.application.port.in.DefineScheduleUseCase;
import es.ulpgc.control_presencia.backend.application.port.in.ScheduleDefinition;
import es.ulpgc.control_presencia.backend.application.port.in.ScheduleType;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.domain.Person;
import es.ulpgc.control_presencia.backend.domain.Shift;
import es.ulpgc.control_presencia.backend.domain.TimeSlot;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/** Seeds three example schedules and three fictional people. Disable with app.seed-data=false. */
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {
    private static final LocalDate START = LocalDate.of(2026, 1, 1);

    private final PersonRepository persons;
    private final DefineScheduleUseCase define;
    private final AssignScheduleUseCase assign;

    public DataInitializer(PersonRepository persons, DefineScheduleUseCase define, AssignScheduleUseCase assign) {
        this.persons = persons;
        this.define = define;
        this.assign = assign;
    }

    @Override
    public void run(String... args) {
        if (persons.findById("ana").isPresent()) return;   // already seeded

        persons.save(new Person("ana", "Ana (oficina)"));
        persons.save(new Person("luis", "Luis (restaurante)"));
        persons.save(new Person("marta", "Marta (hospital)"));


        define.define("office", new ScheduleDefinition(ScheduleType.CONTINUOUS, "Oficina corrido",
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                List.of(slot(8, 0, 15, 0)), null, null));


        define.define("restaurant", new ScheduleDefinition(ScheduleType.SPLIT, "Restaurante partido",
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                List.of(slot(12, 0, 16, 0), slot(19, 0, 23, 0)), null, null));


        define.define("hospital", new ScheduleDefinition(ScheduleType.SHIFTS, "Hospital turnos y guardias",
                null, null, START,
                List.of(Shift.of("MORNING", slot(8, 0, 15, 0)),
                        Shift.of("AFTERNOON", slot(15, 0, 22, 0)),
                        Shift.of("ON_CALL", slot(20, 0, 8, 0)),
                        Shift.off(),
                        Shift.off())));

        assign.assign("ana", "office", START, null);
        assign.assign("luis", "restaurant", START, null);
        assign.assign("marta", "hospital", START, null);
    }

    private static TimeSlot slot(int startH, int startM, int endH, int endM) {
        return new TimeSlot(LocalTime.of(startH, startM), LocalTime.of(endH, endM));
    }
}
