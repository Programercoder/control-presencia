package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "schedules")
public class ScheduleEntity {
    @Id
    private String id;
    private String type;
    private String name;
    @Column(length = 100)
    private String days;
    @Column(length = 500)
    private String slots;
    private LocalDate cycleStart;
    @Column(length = 2000)
    private String cycle;

    protected ScheduleEntity() { }

    public ScheduleEntity(String id, String type, String name, String days, String slots,
                          LocalDate cycleStart, String cycle) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.days = days;
        this.slots = slots;
        this.cycleStart = cycleStart;
        this.cycle = cycle;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public String getDays() { return days; }
    public String getSlots() { return slots; }
    public LocalDate getCycleStart() { return cycleStart; }
    public String getCycle() { return cycle; }
}
