package es.ulpgc.control_presencia.backend.domain;

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

    @Override
    public String name() {
        return name;
    }

    public Shift shiftOn(LocalDate date) {
        long days = ChronoUnit.DAYS.between(cycleStart, date);
        if (days < 0) return Shift.off();
        return cycle.get((int) (days % cycle.size()));
    }

    @Override
    public List<TimeSlot> slotsFor(LocalDate date) {
        return shiftOn(date).slots();
    }
}
