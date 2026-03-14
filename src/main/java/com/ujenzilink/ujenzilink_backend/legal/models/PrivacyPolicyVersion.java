package com.ujenzilink.ujenzilink_backend.legal.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "privacy_policy_versions")
public class PrivacyPolicyVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "privacy_policy_id", nullable = false, updatable = false)
    private PrivacyPolicy privacyPolicy;

    @Column(nullable = false, updatable = false, length = 50)
    private String version;

    @Column(nullable = false, updatable = false, length = 255)
    private String title;

    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String content;

    @Column(updatable = false, columnDefinition = "TEXT")
    private String changeSummary;

    @Column(nullable = false, updatable = false)
    private int revisionNumber;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant archivedAt;

    @Column(updatable = false)
    private Instant publishedAt;

    public PrivacyPolicyVersion() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PrivacyPolicy getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(PrivacyPolicy privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
