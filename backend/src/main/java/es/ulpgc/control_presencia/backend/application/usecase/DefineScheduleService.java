package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.DefineScheduleUseCase;
import es.ulpgc.control_presencia.backend.application.port.in.ScheduleDefinition;
import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.domain.DomainException;
import es.ulpgc.control_presencia.backend.domain.DuplicateScheduleException;

public class DefineScheduleService implements DefineScheduleUseCase {
    private final ScheduleRepository schedules;

    public DefineScheduleService(ScheduleRepository schedules) {
        this.schedules = schedules;
    }

    @Override
    public void define(String scheduleId, ScheduleDefinition definition) {
        if (scheduleId == null || scheduleId.isBlank()) throw new DomainException("Missing schedule id");
        if (definition == null) throw new DomainException("Missing schedule definition");
        if (schedules.findById(scheduleId).isPresent()) throw new DuplicateScheduleException(scheduleId);
        schedules.save(scheduleId, ScheduleFactory.create(definition));
    }
}
