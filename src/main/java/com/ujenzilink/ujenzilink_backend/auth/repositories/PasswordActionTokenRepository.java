package com.ujenzilink.ujenzilink_backend.auth.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.PasswordActionToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordActionTokenRepository extends CrudRepository<PasswordActionToken, String> {
    List<PasswordActionToken> findByUserId(UUID userId);
}
