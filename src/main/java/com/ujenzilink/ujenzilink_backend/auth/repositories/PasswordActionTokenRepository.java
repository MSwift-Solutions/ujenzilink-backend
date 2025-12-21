package com.ujenzilink.ujenzilink_backend.auth.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.PasswordActionToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordActionTokenRepository extends CrudRepository<PasswordActionToken, String> {
    List<PasswordActionToken> findByUserId(Long userId);
}
