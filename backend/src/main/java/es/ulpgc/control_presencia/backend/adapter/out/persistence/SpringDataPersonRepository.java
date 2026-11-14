package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPersonRepository extends JpaRepository<PersonEntity, String> { }
