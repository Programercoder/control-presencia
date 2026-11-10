package es.ulpgc.control_presencia.backend.domain;

import java.util.List;

public record Shift(String name, List<TimeSlot> slots) {
    public Shift {
        if (name == null || name.isBlank()) throw new DomainException("A shift needs a name");
        slots = List.copyOf(slots);
    }

    public static Shift of(String name, TimeSlot slot) {
        return new Shift(name, List.of(slot));
    }

    public static Shift off() {
        return new Shift("OFF", List.of());
    }
}
