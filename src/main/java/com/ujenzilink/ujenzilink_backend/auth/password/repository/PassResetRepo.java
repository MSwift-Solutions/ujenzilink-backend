package com.ujenzilink.ujenzilink_backend.auth.password.repository;

import eng.musa.m_swift_backend.authentication.models.User;
import eng.musa.m_swift_backend.authentication.password.model.ForgotPassReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassResetRepo extends JpaRepository<ForgotPassReset, Long> {

    ForgotPassReset findTopByUserOrderByPassResetDateDesc(User user);

}
