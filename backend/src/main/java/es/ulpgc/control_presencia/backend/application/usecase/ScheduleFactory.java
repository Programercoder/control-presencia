package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.ScheduleDefinition;
import es.ulpgc.control_presencia.backend.domain.*;

public final class ScheduleFactory {
    private ScheduleFactory() { }

    public static Schedule create(ScheduleDefinition d) {
        if (d.type() == null) throw new DomainException("Missing schedule type");
        return switch (d.type()) {
            case CONTINUOUS -> {
                if (d.slots().size() != 1) {
                    throw new DomainException("A continuous schedule needs exactly one slot");
                }
                yield new ContinuousSchedule(d.name(), d.days(), d.slots().get(0));
            }
            case SPLIT -> new SplitSchedule(d.name(), d.days(), d.slots());
            case SHIFTS -> new ShiftSchedule(d.name(), d.cycleStart(), d.cycle());
        };
    }
}
