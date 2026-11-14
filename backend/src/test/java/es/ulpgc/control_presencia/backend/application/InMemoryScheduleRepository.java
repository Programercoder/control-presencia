package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.domain.Schedule;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryScheduleRepository implements ScheduleRepository {
    private final Map<String, Schedule> data = new LinkedHashMap<>();

    @Override
    public Optional<Schedule> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Map<String, Schedule> findAll() {
        return new LinkedHashMap<>(data);
    }

    @Override
    public void save(String id, Schedule schedule) {
        data.put(id, schedule);
    }
}
