package com.ujenzilink.ujenzilink_backend.auth.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.VerificationToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationTokenRepository extends CrudRepository<VerificationToken, String> {
    List<VerificationToken> findByUserId(Long userId);
}
