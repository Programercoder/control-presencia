package es.ulpgc.control_presencia.backend.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public final class PresenceEvaluator {
    private final long toleranceMinutes;

    public PresenceEvaluator(long toleranceMinutes) { this.toleranceMinutes = toleranceMinutes; }

    public PresenceResult evaluate(Schedule schedule, LocalDate date, List<ClockEvent> events, LocalDateTime now) {
        List<TimeSlot> slots = schedule.slotsFor(date);
        List<ClockEvent> sorted = events.stream().sorted(Comparator.comparing(ClockEvent::timestamp)).toList();

        long expected = slots.stream().mapToLong(s -> s.duration().toMinutes()).sum();
        boolean open = !sorted.isEmpty() && sorted.get(sorted.size() - 1).type() == ClockEventType.CLOCK_IN;
        long worked = workedMinutes(sorted, now);
        long deviation = worked - expected;

        if (slots.isEmpty()) {
            return new PresenceResult(PresenceStatus.NO_SHIFT, 0, worked, worked, List.of());
        }

        LocalDateTime start = slots.get(0).startOn(date);
        LocalDateTime end = slots.get(slots.size() - 1).endOn(date);
        LocalDateTime arrivalLimit = start.plusMinutes(toleranceMinutes);
        LocalDateTime departureLimit = end.minusMinutes(toleranceMinutes);
        List<Incident> incidents = new ArrayList<>();


        if (sorted.isEmpty()) {
            PresenceStatus status;
            if (!now.isAfter(arrivalLimit)) {
                status = PresenceStatus.PENDING;
            } else if (now.isBefore(end)) {
                status = PresenceStatus.LATE;
                incidents.add(new Incident(IncidentType.LATE_ARRIVAL,
                        Duration.between(start, now).toMinutes(), "Has not clocked in yet"));
            } else {
                status = PresenceStatus.ABSENT;
                incidents.add(new Incident(IncidentType.ABSENCE, expected, "No clock events in the shift"));
            }
            return new PresenceResult(status, expected, 0, -expected, incidents);
        }

        // Late arrival on the first clock-in
        ClockEvent first = sorted.get(0);
        if (first.timestamp().isAfter(arrivalLimit)) {
            incidents.add(new Incident(IncidentType.LATE_ARRIVAL,
                    Duration.between(start, first.timestamp()).toMinutes(), "Late arrival"));
        }

        PresenceStatus status;
        if (open) {
            if (now.isAfter(end.plusMinutes(toleranceMinutes))) {
                status = PresenceStatus.CLOCK_OUT_PENDING;
                incidents.add(new Incident(IncidentType.CLOCK_OUT_PENDING, 0, "Clock-out was not recorded"));
            } else {
                status = PresenceStatus.PRESENT;
            }
        } else {
            ClockEvent lastEvent = sorted.get(sorted.size() - 1);
            if (lastEvent.timestamp().isBefore(departureLimit)) {
                incidents.add(new Incident(IncidentType.EARLY_DEPARTURE,
                        Duration.between(lastEvent.timestamp(), end).toMinutes(), "Early departure"));
            }
            status = incidents.isEmpty() ? PresenceStatus.COMPLETED : PresenceStatus.WITH_INCIDENT;
        }
        return new PresenceResult(status, expected, worked, deviation, incidents);
    }


    private long workedMinutes(List<ClockEvent> sorted, LocalDateTime now) {
        long total = 0;
        LocalDateTime clockIn = null;
        for (ClockEvent e : sorted) {
            if (e.type() == ClockEventType.CLOCK_IN) {
                clockIn = e.timestamp();
            } else if (clockIn != null) {
                total += Duration.between(clockIn, e.timestamp()).toMinutes();
                clockIn = null;
            }
        }
        if (clockIn != null && now.isAfter(clockIn)) {
            total += Duration.between(clockIn, now).toMinutes();
        }
        return total;
    }
}
