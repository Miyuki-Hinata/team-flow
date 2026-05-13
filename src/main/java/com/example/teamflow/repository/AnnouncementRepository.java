package com.example.teamflow.repository;

import com.example.teamflow.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByDeletedAtIsNull();

    Optional<Announcement> findByIdAndDeletedAtIsNull(Long id);
}