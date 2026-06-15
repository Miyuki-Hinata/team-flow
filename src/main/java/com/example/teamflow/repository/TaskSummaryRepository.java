package com.example.teamflow.repository;

import com.example.teamflow.entity.TaskSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskSummaryRepository extends JpaRepository<TaskSummary, Long> {
    Optional<TaskSummary> findByPatient_Id(Long patientId);
}
