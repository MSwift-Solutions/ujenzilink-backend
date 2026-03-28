package com.ujenzilink.ujenzilink_backend.auth.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findFirstByEmail(String email);

    User findFirstByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findById(UUID userId);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', COALESCE(u.middleName, ''), ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
            +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND u.isDeleted = false AND u.isEnabled = true")
    List<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    List<User> findByIsDeletedTrueOrderByDeletedAtDesc();

    long countByIsDeletedFalse();

    long countByIsDeletedTrue();

    long countByDateOfCreationAfter(Instant date);

    List<User> findByIsEnabledFalseAndIsDeletedFalseOrderByDateOfCreationDesc();
}
