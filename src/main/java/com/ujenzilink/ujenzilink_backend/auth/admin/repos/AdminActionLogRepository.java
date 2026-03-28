package com.ujenzilink.ujenzilink_backend.auth.admin.repos;

import com.ujenzilink.ujenzilink_backend.auth.admin.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, UUID> {
}
