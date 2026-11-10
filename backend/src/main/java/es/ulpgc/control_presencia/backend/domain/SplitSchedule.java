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

    @Override public String name() { return name; }

    @Override public List<TimeSlot> slotsFor(LocalDate date) {
        return days.contains(date.getDayOfWeek()) ? slots : List.of();
    }
}

Shift.java (one day of the cycle: morning, afternoon, night, on-call or off)

java
package es.ulpgc.presencia.backend.domain;

import java.util.List;

public record Shift(String name, List<TimeSlot> slots) {
    public Shift {
        if (name == null || name.isBlank()) throw new DomainException("A shift needs a name");
        slots = List.copyOf(slots);
    }

    public static Shift of(String name, TimeSlot slot) { return new Shift(name, List.of(slot)); }

    public static Shift off() { return new Shift("OFF", List.of()); }
}

ShiftSchedule.java (hospital: rotating cycle of shifts and on-call duty)

java
package es.ulpgc.presencia.backend.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


public final class ShiftSchedule implements Schedule {
    private final String name;
    private final LocalDate cycleStart;
    private final List<Shift> cycle;

    public ShiftSchedule(String name, LocalDate cycleStart, List<Shift> cycle) {
        if (name == null || name.isBlank()) throw new DomainException("A schedule needs a name");
        if (cycleStart == null) throw new DomainException("Missing cycle start date");
        if (cycle == null || cycle.isEmpty()) throw new DomainException("The cycle cannot be empty");
        this.name = name;
        this.cycleStart = cycleStart;
        this.cycle = List.copyOf(cycle);
    }

    @Override public String name() { return name; }

    public Shift shiftOn(LocalDate date) {
        long days = ChronoUnit.DAYS.between(cycleStart, date);
        if (days < 0) return Shift.off();
        return cycle.get((int) (days % cycle.size()));
    }

    @Override public List<TimeSlot> slotsFor(LocalDate date) {
        return shiftOn(date).slots();
    }
}
