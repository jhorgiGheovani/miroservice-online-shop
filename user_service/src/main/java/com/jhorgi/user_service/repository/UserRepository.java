package com.jhorgi.user_service.repository;

import com.jhorgi.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    java.util.Optional<User> findByUsername(String username);
}
