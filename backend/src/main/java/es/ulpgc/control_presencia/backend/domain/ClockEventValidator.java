package es.ulpgc.control_presencia.backend.domain;

import java.util.Optional;

public final class ClockEventValidator {

    /** @param last the last clock event already stored for that person (if any) */
    public void validate(Optional<ClockEvent> last, ClockEvent next) {
        if (last.isEmpty()) {
            if (next.type() == ClockEventType.CLOCK_OUT) {
                throw new InconsistentClockEventException("Cannot clock out without a previous clock-in");
            }
            return;
        }
        ClockEvent previous = last.get();
        if (previous.type() == next.type() && previous.timestamp().equals(next.timestamp())) {
            throw new DuplicateClockEventException("Duplicate clock event");
        }
        if (next.timestamp().isBefore(previous.timestamp())) {
            throw new InconsistentClockEventException("The event is earlier than the last recorded one");
        }
        if (previous.type() == next.type()) {
            throw new InconsistentClockEventException(
                    next.type() == ClockEventType.CLOCK_IN
                            ? "There is already an open clock-in: clock out first"
                            : "There is no open clock-in: clock in first");
        }
    }
}
