package com.ujenzilink.ujenzilink_backend.auth.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
}
