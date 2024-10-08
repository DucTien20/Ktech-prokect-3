package com.example.ktech_project3.repo;

import com.example.project_3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByBusinessApplicationTrueAndAuthorities(String authorities);

}
