package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.ListSchedulesUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.domain.Schedule;

import java.util.Map;

public class ListSchedulesService implements ListSchedulesUseCase {
    private final ScheduleRepository schedules;

    public ListSchedulesService(ScheduleRepository schedules) {
        this.schedules = schedules;
    }

    @Override
    public Map<String, Schedule> list() {
        return schedules.findAll();
    }
}
