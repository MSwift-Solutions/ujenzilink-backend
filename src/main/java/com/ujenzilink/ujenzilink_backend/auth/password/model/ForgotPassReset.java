package com.ujenzilink.ujenzilink_backend.auth.password.model;

import eng.musa.m_swift_backend.authentication.enums.Roles;
import eng.musa.m_swift_backend.authentication.models.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "forgot_pass")
public class ForgotPassReset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime passResetDate;
    private Roles initiatedBy;
    private Boolean passwordChanged = false;
    @ManyToOne
    @JoinColumn(
            nullable = false,
            name = "user_id"
    )
    private User user;
}
