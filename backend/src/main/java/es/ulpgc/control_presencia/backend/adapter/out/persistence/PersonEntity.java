package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "persons")
public class PersonEntity {
    @Id
    private String id;
    private String name;

    protected PersonEntity() { }

    public PersonEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
