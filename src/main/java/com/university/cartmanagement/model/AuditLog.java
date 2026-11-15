package com.university.cartmanagement.model;

import java.time.LocalDateTime;

/**
 * AuditLog entity used to demonstrate Traditional JDBC logging & transaction tracking.
 */
public class AuditLog extends BaseEntity {

    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private String performedBy;
    private LocalDateTime timestamp;

    public AuditLog() {
        super();
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String action, String entityType, Long entityId, String details, String performedBy) {
        this(null, action, entityType, entityId, details, performedBy, LocalDateTime.now());
    }

    public AuditLog(Long id, String action, String entityType, Long entityId,
                    String details, String performedBy, LocalDateTime timestamp) {
        super(id);
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.performedBy = performedBy;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", performedBy='" + performedBy + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
