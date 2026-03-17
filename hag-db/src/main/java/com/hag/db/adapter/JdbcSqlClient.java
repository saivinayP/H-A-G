package com.hag.db.adapter;

import com.hag.core.adapter.DbClient;
import com.hag.core.db.DbQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * JdbcSqlClient — JDBC-backed implementation of {@link DbClient}.
 *
 * <p>Supports any JDBC-compatible database (MySQL, MS SQL Server, H2, PostgreSQL, etc.)
 * via the JDBC driver on the classpath.
 *
 * <h3>Connection lifecycle</h3>
 * The connection is opened lazily on the first query/execute call and cached until
 * {@link #close()} is explicitly called (typically during suite teardown).
 *
 * <h3>Thread-safety</h3>
 * Each test thread should have its own {@code JdbcSqlClient} instance (managed by
 * {@link com.hag.core.db.DbClientRegistry} held in a {@code ThreadLocal}).
 *
 * <h3>Config (testdata.config.yml)</h3>
 * <pre>
 * database:                    # single / default profile
 *   url:      jdbc:mysql://localhost:3306/testdb
 *   username: qa_user
 *   password: ${env.DB_PASSWORD}
 *
 * databases:                   # named profiles (Phase 4)
 *   orders_db:
 *     url: jdbc:mysql://...
 * </pre>
 */
public class JdbcSqlClient implements DbClient {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcSqlClient.class);

    private final String url;
    private final String username;
    private final String password;

    private Connection    connection;
    private DbQueryResult lastQueryResult;
    private int           lastAffectedRows = -1;

    public JdbcSqlClient(String url, String username, String password) {
        this.url      = Objects.requireNonNull(url,      "DB url must not be null");
        this.username = Objects.requireNonNull(username, "DB username must not be null");
        this.password = password != null ? password : "";
    }

    // ── DbAdapter (query & execute) ───────────────────────────────────────

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        LOG.info("DB → QUERY: {}", abbreviate(sql, 120));
        List<Map<String, Object>> rows        = new ArrayList<>();
        List<String>              columnNames = new ArrayList<>();

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                columnNames.add(meta.getColumnLabel(i));
            }
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String col : columnNames) {
                    row.put(col, rs.getObject(col));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed: " + e.getMessage(), e);
        }

        lastQueryResult = new DbQueryResult(rows, columnNames);
        LOG.info("DB ← {} rows returned", lastQueryResult.rowCount());
        return rows;
    }

    @Override
    public int executeUpdate(String sql) {
        LOG.info("DB → EXECUTE: {}", abbreviate(sql, 120));
        try (Statement stmt = getConnection().createStatement()) {
            lastAffectedRows = stmt.executeUpdate(sql);
            LOG.info("DB ← {} row(s) affected", lastAffectedRows);
            return lastAffectedRows;
        } catch (SQLException e) {
            throw new RuntimeException("DB execute failed: " + e.getMessage(), e);
        }
    }

    // ── DbClient (result accessors & lifecycle) ───────────────────────────

    @Override
    public DbQueryResult getLastQueryResult() {
        if (lastQueryResult == null) {
            throw new IllegalStateException(
                "No DB_QUERY result available. "
                + "ASSERT_ROW_COUNT, ASSERT_COLUMN, and STORE_DATA:DB require "
                + "a preceding DB_QUERY step in the same test.");
        }
        return lastQueryResult;
    }

    @Override
    public int getLastAffectedRows() {
        return lastAffectedRows;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    LOG.info("DB → Connection closed [{}]", url);
                }
            } catch (SQLException e) {
                LOG.warn("DB → Failed to close connection: {}", e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    /** Lazily opens and caches the JDBC connection. */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            LOG.info("DB → Opening connection to: {}", url);
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    private static String abbreviate(String s, int max) {
        if (s == null)        return "(null)";
        if (s.length() <= max) return s;
        return s.substring(0, max) + "…";
    }
}
