package es.ulpgc.control_presencia.backend.config;

import es.ulpgc.control_presencia.backend.application.port.out.AssignmentRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ClockEventRepository;
import es.ulpgc.control_presencia.backend.application.port.out.PersonRepository;
import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.application.port.out.TimeProvider;
import es.ulpgc.control_presencia.backend.application.usecase.*;
import es.ulpgc.control_presencia.backend.domain.PresenceEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    PresenceEvaluator presenceEvaluator(@Value("${presence.tolerance-minutes:5}") long tolerance) {
        return new PresenceEvaluator(tolerance);
    }

    @Bean
    RegisterClockEventService registerClockEventService(PersonRepository persons, AssignmentRepository assignments,
                                                        ClockEventRepository events, TimeProvider time) {
        return new RegisterClockEventService(persons, assignments, events, time);
    }

    @Bean
    GetPresenceStatusService getPresenceStatusService(PersonRepository persons, AssignmentRepository assignments,
                                                      ScheduleRepository schedules, ClockEventRepository events,
                                                      TimeProvider time, PresenceEvaluator evaluator) {
        return new GetPresenceStatusService(persons, assignments, schedules, events, time, evaluator);
    }

    @Bean
    DefineScheduleService defineScheduleService(ScheduleRepository schedules) {
        return new DefineScheduleService(schedules);
    }

    @Bean
    AssignScheduleService assignScheduleService(PersonRepository persons, ScheduleRepository schedules,
                                                AssignmentRepository assignments) {
        return new AssignScheduleService(persons, schedules, assignments);
    }

    @Bean
    GetClockEventsService getClockEventsService(PersonRepository persons, ClockEventRepository events) {
        return new GetClockEventsService(persons, events);
    }

    @Bean
    ListPersonsService listPersonsService(PersonRepository persons) {
        return new ListPersonsService(persons);
    }

    @Bean
    ListSchedulesService listSchedulesService(ScheduleRepository schedules) {
        return new ListSchedulesService(schedules);
    }
}
