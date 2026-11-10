package es.ulpgc.control_presencia.backend.application;

import es.ulpgc.control_presencia.backend.application.port.out.ClockEventRepository;
import es.ulpgc.control_presencia.backend.domain.ClockEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryClockEventRepository implements ClockEventRepository {
    private final List<ClockEvent> data = new ArrayList<>();

    @Override
    public Optional<ClockEvent> findLast(String personId) {
        return data.stream()
                .filter(e -> e.personId().equals(personId))
                .reduce((first, second) -> second);
    }

    @Override
    public List<ClockEvent> findBetween(String personId, LocalDateTime from, LocalDateTime to) {
        return data.stream()
                .filter(e -> e.personId().equals(personId))
                .filter(e -> !e.timestamp().isBefore(from) && e.timestamp().isBefore(to))
                .toList();
    }

    @Override
    public void save(ClockEvent event) {
        data.add(event);
    }
}
