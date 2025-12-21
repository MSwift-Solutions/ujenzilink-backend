package com.ujenzilink.ujenzilink_backend.auth.password.model;

import eng.musa.m_swift_backend.authentication.enums.Roles;
import eng.musa.m_swift_backend.authentication.models.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "pass_change")
@Data
public class PassChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime passChangeDate;
    private Roles initiatedBy;
    @ManyToOne
    @JoinColumn(
            nullable = false,
            name = "user_id"
    )
    private User user;
}
