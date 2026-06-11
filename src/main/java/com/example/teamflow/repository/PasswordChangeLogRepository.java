package com.example.teamflow.repository;

import com.example.teamflow.entity.PasswordChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordChangeLogRepository extends JpaRepository<PasswordChangeLog, Long> {
}
