package com.ujenzilink.ujenzilink_backend.auth.password.repository;

import eng.musa.m_swift_backend.authentication.password.model.PassChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassChangeRepo extends JpaRepository<PassChange, Long> {
}
