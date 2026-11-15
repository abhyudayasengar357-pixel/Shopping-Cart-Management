package com.university.cartmanagement.repository;

import com.university.cartmanagement.model.AuditLog;

import java.util.List;

/**
 * Repository interface for system audit logging.
 */
public interface AuditRepository {

    void logAction(AuditLog log);

    List<AuditLog> findRecent(int limit);
}
