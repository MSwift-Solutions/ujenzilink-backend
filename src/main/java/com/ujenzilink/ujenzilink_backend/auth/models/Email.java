package com.ujenzilink.ujenzilink_backend.auth.models;

import com.ujenzilink.ujenzilink_backend.auth.enums.EmailTypes;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "emails")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    private EmailTypes emailType;

    @Column(columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Instant sentAt;

    public Email() {
    }

    public Email(String recipientEmail, EmailTypes emailType, String body, User user) {
        this.recipientEmail = recipientEmail;
        this.emailType = emailType;
        this.body = body;
        this.user = user;
        this.sentAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public EmailTypes getEmailType() {
        return emailType;
    }

    public void setEmailType(EmailTypes emailType) {
        this.emailType = emailType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
