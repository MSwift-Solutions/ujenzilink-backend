package com.ujenzilink.ujenzilink_backend.auth.password.model;

import com.ujenzilink.ujenzilink_backend.auth.enums.PasswordActionType;
import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "password_actions")
public class PasswordAction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PasswordActionType actionType;

    @Column(nullable = false)
    private Instant actionDate;

    @Enumerated(EnumType.STRING)
    private Roles initiatedBy;

    @Column(nullable = false)
    private boolean completed = false;

    private boolean codeConfirmed = false;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public PasswordAction() {
    }

    public PasswordAction(PasswordActionType actionType, Instant actionDate, Roles initiatedBy, Boolean completed,
            User user) {
        this.actionType = actionType;
        this.actionDate = actionDate;
        this.initiatedBy = initiatedBy;
        this.completed = completed;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PasswordActionType getActionType() {
        return actionType;
    }

    public void setActionType(PasswordActionType actionType) {
        this.actionType = actionType;
    }

    public Instant getActionDate() {
        return actionDate;
    }

    public void setActionDate(Instant actionDate) {
        this.actionDate = actionDate;
    }

    public Roles getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(Roles initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public  void setCodeConfirmed(Boolean codeConfirmed) {
        this.codeConfirmed = codeConfirmed;
    }
    public Boolean getCodeConfirmed() {return codeConfirmed;}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
