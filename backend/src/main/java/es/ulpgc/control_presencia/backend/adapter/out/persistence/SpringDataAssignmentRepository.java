package es.ulpgc.control_presencia.backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAssignmentRepository extends JpaRepository<AssignmentEntity, Long> {
    List<AssignmentEntity> findByPersonId(String personId);
}
