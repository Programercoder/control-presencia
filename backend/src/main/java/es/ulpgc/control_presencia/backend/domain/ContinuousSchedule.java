package es.ulpgc.control_presencia.backend.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public final class ContinuousSchedule implements Schedule {
    private final String name;
    private final Set<DayOfWeek> days;
    private final TimeSlot slot;

    public ContinuousSchedule(String name, Set<DayOfWeek> days, TimeSlot slot) {
        if (name == null || name.isBlank()) throw new DomainException("A schedule needs a name");
        if (days == null || days.isEmpty()) throw new DomainException("Provide at least one working day");
        if (slot == null) throw new DomainException("Missing time slot");
        this.name = name;
        this.days = Set.copyOf(days);
        this.slot = slot;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<TimeSlot> slotsFor(LocalDate date) {
        return days.contains(date.getDayOfWeek()) ? List.of(slot) : List.of();
    }

    public Set<DayOfWeek> days() {
        return days;
    }

    public TimeSlot slot() {
        return slot;
    }
}
