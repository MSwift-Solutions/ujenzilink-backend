package com.ujenzilink.ujenzilink_backend.user_mgt.repositories;

import com.ujenzilink.ujenzilink_backend.user_mgt.models.Bio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BioRepository extends JpaRepository<Bio, UUID> {
}
