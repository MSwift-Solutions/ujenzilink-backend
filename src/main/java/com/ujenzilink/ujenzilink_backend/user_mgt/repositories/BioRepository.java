package com.ujenzilink.ujenzilink_backend.user_mgt.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.user_mgt.models.Bio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BioRepository extends JpaRepository<Bio, UUID> {
    Optional<Bio> findByUser(User user);
}
