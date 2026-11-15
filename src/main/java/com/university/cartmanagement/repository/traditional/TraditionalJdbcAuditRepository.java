package com.university.cartmanagement.repository.traditional;

import com.university.cartmanagement.exception.DatabaseOperationException;
import com.university.cartmanagement.model.AuditLog;
import com.university.cartmanagement.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Traditional JDBC implementation of AuditRepository.
 * Explicitly demonstrates Manual JDBC Transaction Handling:
 *  - conn.setAutoCommit(false)
 *  - conn.commit()
 *  - conn.rollback() in catch block
 *  - Explicit resource closing
 */
@Repository("traditionalJdbcAuditRepository")
public class TraditionalJdbcAuditRepository implements AuditRepository {

    private static final Logger log = LoggerFactory.getLogger(TraditionalJdbcAuditRepository.class);

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String dbDriver;

    public TraditionalJdbcAuditRepository(
            @Value("${app.traditional.jdbc.url:${spring.datasource.url}}") String dbUrl,
            @Value("${app.traditional.jdbc.username:${spring.datasource.username}}") String dbUser,
            @Value("${app.traditional.jdbc.password:${spring.datasource.password:}}") String dbPassword,
            @Value("${app.traditional.jdbc.driver:${spring.datasource.driver-class-name:org.h2.Driver}}") String dbDriver) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbDriver = dbDriver;

        try {
            Class.forName(this.dbDriver);
        } catch (ClassNotFoundException e) {
            log.warn("JDBC Driver class not found: {}", this.dbDriver);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    @Override
    public void logAction(AuditLog audit) {
        String sql = "INSERT INTO audit_logs (action, entity_type, entity_id, details, performed_by, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            // 1. Begin Traditional JDBC Transaction
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(sql);
            ps.setString(1, audit.getAction());
            ps.setString(2, audit.getEntityType());
            if (audit.getEntityId() != null) {
                ps.setLong(3, audit.getEntityId());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setString(4, audit.getDetails());
            ps.setString(5, audit.getPerformedBy() != null ? audit.getPerformedBy() : "SYSTEM");
            ps.setTimestamp(6, Timestamp.valueOf(audit.getTimestamp() != null ? audit.getTimestamp() : LocalDateTime.now()));

            ps.executeUpdate();

            // 2. Commit Transaction
            conn.commit();
            log.debug("Traditional JDBC Transaction committed for audit action: {}", audit.getAction());

        } catch (SQLException e) {
            // 3. Rollback Transaction on Error
            if (conn != null) {
                try {
                    log.warn("Rolling back Traditional JDBC transaction due to error: {}", e.getMessage());
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Failed to rollback Traditional JDBC transaction", ex);
                }
            }
            throw new DatabaseOperationException("Traditional JDBC transaction failed for audit logging", sql, e);
        } finally {
            // 4. Manually close PreparedStatement and Connection
            if (ps != null) {
                try { ps.close(); } catch (SQLException ignored) {}
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public List<AuditLog> findRecent(int limit) {
        String sql = "SELECT id, action, entity_type, entity_id, details, performed_by, timestamp " +
                     "FROM audit_logs ORDER BY id DESC LIMIT ?";
        List<AuditLog> logs = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 20);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog logEntry = new AuditLog();
                    logEntry.setId(rs.getLong("id"));
                    logEntry.setAction(rs.getString("action"));
                    logEntry.setEntityType(rs.getString("entity_type"));
                    long entityId = rs.getLong("entity_id");
                    if (!rs.wasNull()) {
                        logEntry.setEntityId(entityId);
                    }
                    logEntry.setDetails(rs.getString("details"));
                    logEntry.setPerformedBy(rs.getString("performed_by"));
                    Timestamp ts = rs.getTimestamp("timestamp");
                    if (ts != null) {
                        logEntry.setTimestamp(ts.toLocalDateTime());
                    }
                    logs.add(logEntry);
                }
            }
            return logs;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in findRecent", sql, e);
        }
    }
}
