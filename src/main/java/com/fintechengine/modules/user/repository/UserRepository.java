package com.fintechengine.modules.user.repository;

import com.fintechengine.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByDocument(String document);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
