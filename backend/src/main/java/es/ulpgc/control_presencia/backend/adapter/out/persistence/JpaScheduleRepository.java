package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import es.ulpgc.control_presencia.backend.application.port.out.ScheduleRepository;
import es.ulpgc.control_presencia.backend.domain.Schedule;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaScheduleRepository implements ScheduleRepository {
    private final SpringDataScheduleRepository jpa;

    public JpaScheduleRepository(SpringDataScheduleRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Schedule> findById(String id) {
        return jpa.findById(id).map(ScheduleMapper::toDomain);
    }

    @Override
    public Map<String, Schedule> findAll() {
        return jpa.findAll().stream()
                .sorted(Comparator.comparing(ScheduleEntity::getId))
                .collect(Collectors.toMap(ScheduleEntity::getId, ScheduleMapper::toDomain,
                        (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public void save(String id, Schedule schedule) {
        jpa.save(ScheduleMapper.toEntity(id, schedule));
    }
}
