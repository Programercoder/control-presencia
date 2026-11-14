package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String personId;
    private String scheduleId;
    @Column(name = "valid_from")
    private LocalDate validFrom;
    @Column(name = "valid_to")
    private LocalDate validTo;

    protected AssignmentEntity() { }

    public AssignmentEntity(String personId, String scheduleId, LocalDate validFrom, LocalDate validTo) {
        this.personId = personId;
        this.scheduleId = scheduleId;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public Long getId() { return id; }
    public String getPersonId() { return personId; }
    public String getScheduleId() { return scheduleId; }
    public LocalDate getValidFrom() { return validFrom; }
    public LocalDate getValidTo() { return validTo; }
}
