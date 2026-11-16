package es.ulpgc.control_presencia.backend.application.usecase;

import es.ulpgc.control_presencia.backend.application.port.in.GetPresenceStatusUseCase;
import es.ulpgc.control_presencia.backend.application.port.out.JustificationRepository;
import es.ulpgc.control_presencia.backend.domain.Incident;
import es.ulpgc.control_presencia.backend.domain.Justification;
import es.ulpgc.control_presencia.backend.domain.PresenceResult;

import java.time.LocalDate;
import java.util.List;

public class JustifiedPresenceService implements GetPresenceStatusUseCase {
    private final GetPresenceStatusUseCase delegate;
    private final JustificationRepository justifications;

    public JustifiedPresenceService(GetPresenceStatusUseCase delegate, JustificationRepository justifications) {
        this.delegate = delegate;
        this.justifications = justifications;
    }

    @Override
    public PresenceResult getStatus(String personId, LocalDate date) {
        PresenceResult result = delegate.getStatus(personId, date);
        List<Justification> found = justifications.findByPersonAndDate(personId, date);
        if (found.isEmpty()) return result;

        List<Incident> incidents = result.incidents().stream()
                .map(incident -> found.stream()
                        .filter(j -> j.incidentType() == incident.type())
                        .findFirst()
                        .map(j -> incident.justifiedWith(j.reason()))
                        .orElse(incident))
                .toList();
        return new PresenceResult(result.status(), result.expectedMinutes(), result.workedMinutes(),
                result.deviationMinutes(), incidents);
    }
}
