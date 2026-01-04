package com.ujenzilink.ujenzilink_backend.auth.repositories;


import com.ujenzilink.ujenzilink_backend.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findFirstByEmail(String email);
    User findFirstByUsername(String username);
    boolean existsByUsername(String username);

    Optional<User> findById(UUID userId);
}
