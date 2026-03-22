package com.example.teamflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.teamflow.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
