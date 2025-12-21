package com.ujenzilink.ujenzilink_backend.auth.password.repository;

import com.ujenzilink.ujenzilink_backend.auth.enums.PasswordActionType;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.password.model.PasswordAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordActionRepository extends JpaRepository<PasswordAction, Long> {

    PasswordAction findTopByUserAndActionTypeOrderByActionDateDesc(User user, PasswordActionType actionType);
}
