package es.ulpgc.control_presencia.backend.adapter.in.http;

import es.ulpgc.control_presencia.backend.adapter.in.http.ApiDtos.*;
import es.ulpgc.control_presencia.backend.application.port.in.JustifyIncidentUseCase;
import es.ulpgc.control_presencia.backend.application.port.in.ListJustificationsUseCase;
import es.ulpgc.control_presencia.backend.domain.DomainException;
import es.ulpgc.control_presencia.backend.domain.IncidentType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class JustificationController {
    private final JustifyIncidentUseCase justify;
    private final ListJustificationsUseCase list;

    public JustificationController(JustifyIncidentUseCase justify, ListJustificationsUseCase list) {
        this.justify = justify;
        this.list = list;
    }

    @PostMapping("/justifications")
    public ResponseEntity<JustificationResponse> justify(@RequestBody JustificationRequest request) {
        if (request == null) throw new DomainException("Missing request body");
        String personId = HttpMapper.require(request.personId(), "personId");
        IncidentType type = HttpMapper.parseEnum(IncidentType.class, request.incidentType(), "incidentType");
        String reason = HttpMapper.require(request.reason(), "reason");
        String author = HttpMapper.require(request.author(), "author");
        if (request.date() == null) throw new DomainException("Missing field: date");
        var justification = justify.justify(personId, request.date(), type, reason, author);
        return ResponseEntity.status(HttpStatus.CREATED).body(HttpMapper.toResponse(justification));
    }

    @GetMapping("/justifications")
    public List<JustificationResponse> list(
            @RequestParam("personId") String personId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return list.list(personId, from, to).stream().map(HttpMapper::toResponse).toList();
    }
}
