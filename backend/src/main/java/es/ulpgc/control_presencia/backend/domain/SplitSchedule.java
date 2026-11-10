package es.ulpgc.control_presencia.backend.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class SplitSchedule implements Schedule {
    private final String name;
    private final Set<DayOfWeek> days;
    private final List<TimeSlot> slots;

    public SplitSchedule(String name, Set<DayOfWeek> days, List<TimeSlot> slots) {
        if (name == null || name.isBlank()) throw new DomainException("A schedule needs a name");
        if (days == null || days.isEmpty()) throw new DomainException("Provide at least one working day");
        if (slots == null || slots.size() < 2) throw new DomainException("A split schedule needs at least two slots");
        this.name = name;
        this.days = Set.copyOf(days);
        this.slots = slots.stream().sorted(Comparator.comparing(TimeSlot::start)).toList();
        for (int i = 1; i < this.slots.size(); i++) {
            if (this.slots.get(i).start().isBefore(this.slots.get(i - 1).end())) {
                throw new DomainException("Time slots cannot overlap");
            }
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<TimeSlot> slotsFor(LocalDate date) {
        return days.contains(date.getDayOfWeek()) ? slots : List.of();
    }
}
