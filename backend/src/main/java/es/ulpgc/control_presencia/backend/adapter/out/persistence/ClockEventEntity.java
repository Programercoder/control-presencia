package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import es.ulpgc.control_presencia.backend.domain.ClockEventType;
import es.ulpgc.control_presencia.backend.domain.EventSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;


@Entity
@Table(name = "clock_events")
public class ClockEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(updatable = false)
    private String personId;
    @Column(name = "event_time", updatable = false)
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private ClockEventType type;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_source", updatable = false)
    private EventSource source;
    @Column(updatable = false)
    private LocalDateTime recordedAt;

    protected ClockEventEntity() { }

    public ClockEventEntity(String personId, LocalDateTime timestamp, ClockEventType type, EventSource source) {
        this.personId = personId;
        this.timestamp = timestamp;
        this.type = type;
        this.source = source;
        this.recordedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getPersonId() { return personId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public ClockEventType getType() { return type; }
    public EventSource getSource() { return source; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
