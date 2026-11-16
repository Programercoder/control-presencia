package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import es.ulpgc.control_presencia.backend.domain.IncidentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "justifications")
public class JustificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(updatable = false)
    private String personId;
    @Column(name = "incident_date", updatable = false)
    private LocalDate incidentDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", updatable = false)
    private IncidentType incidentType;
    @Column(length = 300, updatable = false)
    private String reason;
    @Column(updatable = false)
    private String author;
    @Column(updatable = false)
    private LocalDateTime recordedAt;

    protected JustificationEntity() { }

    public JustificationEntity(String personId, LocalDate incidentDate, IncidentType incidentType,
                               String reason, String author, LocalDateTime recordedAt) {
        this.personId = personId;
        this.incidentDate = incidentDate;
        this.incidentType = incidentType;
        this.reason = reason;
        this.author = author;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public String getPersonId() { return personId; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public IncidentType getIncidentType() { return incidentType; }
    public String getReason() { return reason; }
    public String getAuthor() { return author; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
