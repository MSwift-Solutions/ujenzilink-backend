package com.ujenzilink.ujenzilink_backend.auth.admin.repos;

import com.ujenzilink.ujenzilink_backend.auth.admin.AdminLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminLoginHistoryRepository extends JpaRepository<AdminLoginHistory, UUID> {
    List<AdminLoginHistory> findByAdminUserIdOrderByLoginAtDesc(UUID adminUserId);
    long countByAdminUserIdAndSuccessTrue(UUID adminUserId);
    long countByAdminUserIdAndSuccessFalse(UUID adminUserId);
}
