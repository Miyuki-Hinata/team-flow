package com.example.teamflow.repository;

import com.example.teamflow.entity.User;
import com.example.teamflow.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    List<User> findByRole(Role role);
}
