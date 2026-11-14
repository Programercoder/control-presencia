package es.ulpgc.control_presencia.backend.adapter.in.http;

import es.ulpgc.control_presencia.backend.adapter.in.http.ApiDtos.*;
import es.ulpgc.control_presencia.backend.application.port.in.AssignScheduleUseCase;
import es.ulpgc.control_presencia.backend.application.port.in.DefineScheduleUseCase;
import es.ulpgc.control_presencia.backend.application.port.in.ListSchedulesUseCase;
import es.ulpgc.control_presencia.backend.domain.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ScheduleController {
    private final DefineScheduleUseCase define;
    private final AssignScheduleUseCase assign;
    private final ListSchedulesUseCase list;

    public ScheduleController(DefineScheduleUseCase define, AssignScheduleUseCase assign,
                              ListSchedulesUseCase list) {
        this.define = define;
        this.assign = assign;
        this.list = list;
    }

    @GetMapping("/schedules")
    public List<ScheduleResponse> schedules() {
        return list.list().entrySet().stream()
                .map(e -> HttpMapper.toResponse(e.getKey(), e.getValue()))
                .toList();
    }

    @PostMapping("/schedules")
    public ResponseEntity<ScheduleResponse> defineSchedule(@RequestBody ScheduleRequest request) {
        if (request == null) throw new DomainException("Missing request body");
        String id = HttpMapper.require(request.id(), "id");
        define.define(id, HttpMapper.toDefinition(request));
        var created = HttpMapper.toResponse(id, list.list().get(id));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/assignments")
    public ResponseEntity<AssignmentResponse> assign(@RequestBody AssignmentRequest request) {
        if (request == null) throw new DomainException("Missing request body");
        var a = assign.assign(HttpMapper.require(request.personId(), "personId"),
                HttpMapper.require(request.scheduleId(), "scheduleId"), request.from(), request.to());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AssignmentResponse(a.personId(), a.scheduleId(), a.from(), a.to()));
    }
}
