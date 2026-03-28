package com.ujenzilink.ujenzilink_backend.auth.admin.repos;

import com.ujenzilink.ujenzilink_backend.auth.admin.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, UUID> {
    
    @Query("SELECT a FROM AdminActionLog a LEFT JOIN FETCH a.adminUser WHERE a.createdAt < :createdAt ORDER BY a.createdAt DESC")
    List<AdminActionLog> findByCreatedAtBeforeWithAdmin(Instant createdAt, Pageable pageable);

    @Query("SELECT a FROM AdminActionLog a LEFT JOIN FETCH a.adminUser ORDER BY a.createdAt DESC")
    List<AdminActionLog> findAllWithAdmin(Pageable pageable);
}
