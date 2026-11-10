package es.ulpgc.control_presencia.backend.domain;

import java.time.LocalDate;
import java.util.List;

public interface Schedule {
    String name();

    /** Slots that START on that date, ordered. Empty list = day off. */
    List<TimeSlot> slotsFor(LocalDate date);
}
