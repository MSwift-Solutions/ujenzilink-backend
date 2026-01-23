package com.ujenzilink.ujenzilink_backend.projects.models;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.enums.MemberRole;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_members")
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false)
    private boolean canViewProject = true;

    @Column(nullable = false)
    private boolean canManageStages = false;

    @Column(nullable = false)
    private boolean canCreatePosts = false;

    @Column(nullable = false)
    private boolean canUploadDocuments = false;

    @Column(nullable = false)
    private boolean canManageMembers = false;

    @CreationTimestamp
    private Instant joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id")
    private User addedBy;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private Instant deletedAt;

    public ProjectMember() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public boolean isCanViewProject() {
        return canViewProject;
    }

    public void setCanViewProject(boolean canViewProject) {
        this.canViewProject = canViewProject;
    }

    public boolean isCanManageStages() {
        return canManageStages;
    }

    public void setCanManageStages(boolean canManageStages) {
        this.canManageStages = canManageStages;
    }

    public boolean isCanCreatePosts() {
        return canCreatePosts;
    }

    public void setCanCreatePosts(boolean canCreatePosts) {
        this.canCreatePosts = canCreatePosts;
    }

    public boolean isCanUploadDocuments() {
        return canUploadDocuments;
    }

    public void setCanUploadDocuments(boolean canUploadDocuments) {
        this.canUploadDocuments = canUploadDocuments;
    }

    public boolean isCanManageMembers() {
        return canManageMembers;
    }

    public void setCanManageMembers(boolean canManageMembers) {
        this.canManageMembers = canManageMembers;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public User getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(User addedBy) {
        this.addedBy = addedBy;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
