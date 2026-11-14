package es.ulpgc.control_presencia.backend.adapter.in.http;

import es.ulpgc.control_presencia.backend.adapter.in.http.ApiDtos.*;
import es.ulpgc.control_presencia.backend.application.port.in.GetClockEventsUseCase;
import es.ulpgc.control_presencia.backend.application.port.in.GetPresenceStatusUseCase;
import es.ulpgc.control_presencia.backend.application.port.in.RegisterClockEventUseCase;
import es.ulpgc.control_presencia.backend.domain.ClockEventType;
import es.ulpgc.control_presencia.backend.domain.DomainException;
import es.ulpgc.control_presencia.backend.domain.EventSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ClockEventController {
    private final RegisterClockEventUseCase register;
    private final GetClockEventsUseCase events;
    private final GetPresenceStatusUseCase presence;

    public ClockEventController(RegisterClockEventUseCase register, GetClockEventsUseCase events,
                                GetPresenceStatusUseCase presence) {
        this.register = register;
        this.events = events;
        this.presence = presence;
    }

    @PostMapping("/clock-events")
    public ResponseEntity<ClockEventResponse> register(@RequestBody ClockEventRequest request) {
        if (request == null) throw new DomainException("Missing request body");
        String personId = HttpMapper.require(request.personId(), "personId");
        ClockEventType type = HttpMapper.parseEnum(ClockEventType.class, request.type(), "type");
        EventSource source = HttpMapper.parseEnum(EventSource.class, request.source(), "source");
        var event = register.register(personId, type, source);
        return ResponseEntity.status(HttpStatus.CREATED).body(HttpMapper.toResponse(event));
    }

    @GetMapping("/clock-events")
    public List<ClockEventResponse> list(
            @RequestParam("personId") String personId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return events.getEvents(personId, from, to).stream().map(HttpMapper::toResponse).toList();
    }

    @GetMapping("/presence/{personId}")
    public PresenceResponse status(
            @PathVariable("personId") String personId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now();
        return HttpMapper.toResponse(personId, day, presence.getStatus(personId, day));
    }
}
