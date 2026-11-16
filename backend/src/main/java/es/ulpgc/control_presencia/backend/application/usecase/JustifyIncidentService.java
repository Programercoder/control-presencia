package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.GetPresenceStatusUseCase;
import es.ulpgc.control_presencia.backend.application.port.in.JustifyIncidentUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.JustificationRepository;
import es.ulpgc.control_presencia.backend.application.port.out.TimeProvider;
import es.ulpgc.control_presencia.backend.domain.*;

import java.time.LocalDate;

public class JustifyIncidentService implements JustifyIncidentUseCase {
    private final GetPresenceStatusUseCase presence;
    private final JustificationRepository justifications;
    private final TimeProvider time;

    public JustifyIncidentService(GetPresenceStatusUseCase presence, JustificationRepository justifications,
                                  TimeProvider time) {
        this.presence = presence;
        this.justifications = justifications;
        this.time = time;
    }

    @Override
    public Justification justify(String personId, LocalDate date, IncidentType type, String reason, String author) {
        if (date == null) throw new DomainException("Missing date");
        if (type == null) throw new DomainException("Missing incident type");

        // Throws if the person does not exist or has no schedule on that date
        PresenceResult result = presence.getStatus(personId, date);
        Incident incident = result.incidents().stream()
                .filter(i -> i.type() == type)
                .findFirst()
                .orElseThrow(() -> new NoSuchIncidentException(
                        "There is no " + type + " incident for " + personId + " on " + date));
        if (incident.isJustified()) {
            throw new DuplicateJustificationException("That incident is already justified");
        }

        var justification = new Justification(personId, date, type, reason, author, time.now());
        justifications.save(justification);
        return justification;
    }
}
