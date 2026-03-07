package com.hag.db.adapter;

import com.hag.core.adapter.DbAdapter;
import com.hag.db.model.DbQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * JDBC-based implementation of {@link DbAdapter}.
 *
 * <p>Establishes and caches a single {@link Connection} per adapter instance.
 * Supports MySQL and MS SQL Server via the drivers included in {@code hag-db/pom.xml}.
 *
 * <h3>Connection config (from testdata.config.yml)</h3>
 * <pre>
 * database:
 *   url:      jdbc:mysql://localhost:3306/testdb
 *   username: qa_user
 *   password: ${env.DB_PASSWORD}
 * </pre>
 */
public final class JdbcDbAdapter implements DbAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcDbAdapter.class);

    private final String url;
    private final String username;
    private final String password;

    private Connection connection;

    public JdbcDbAdapter(String url, String username, String password) {
        this.url      = Objects.requireNonNull(url,      "DB url must not be null");
        this.username = Objects.requireNonNull(username, "DB username must not be null");
        this.password = password != null ? password : "";
    }

    /** Lazily opens and caches the JDBC connection. */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            LOG.info("DB → Opening connection to: {}", url);
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    /**
     * Executes a SQL SELECT and returns all rows as a {@link DbQueryResult}.
     *
     * <p>This method also stores the result in the adapter so downstream
     * assertion and store actions can retrieve it.
     *
     * @param sql resolved SQL query string
     * @return query result — never {@code null}
     */
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

        DbQueryResult result = new DbQueryResult(rows, columnNames);
        lastQueryResult = result;
        LOG.info("DB ← {} rows returned", result.rowCount());
        return rows;
    }

    /**
     * Executes a SQL INSERT / UPDATE / DELETE statement.
     *
     * @param sql resolved DML SQL string
     * @return number of affected rows
     */
    @Override
    public int executeUpdate(String sql) {
        LOG.info("DB → EXECUTE: {}", abbreviate(sql, 120));
        try (Statement stmt = getConnection().createStatement()) {
            int affected = stmt.executeUpdate(sql);
            LOG.info("DB ← {} row(s) affected", affected);
            lastAffectedRows = affected;
            return affected;
        } catch (SQLException e) {
            throw new RuntimeException("DB execute failed: " + e.getMessage(), e);
        }
    }

    // ── Last-result accessors used by assertion/store actions ─────────

    private DbQueryResult lastQueryResult;
    private int           lastAffectedRows = -1;

    public DbQueryResult getLastQueryResult() { return lastQueryResult; }
    public int           getLastAffectedRows()  { return lastAffectedRows; }

    /** Closes the JDBC connection. Called during framework teardown. */
    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    LOG.info("DB → Connection closed");
                }
            } catch (SQLException e) {
                LOG.warn("DB → Failed to close connection: {}", e.getMessage());
            }
        }
    }

    private static String abbreviate(String s, int max) {
        if (s == null)       return "(null)";
        if (s.length() <= max) return s;
        return s.substring(0, max) + "…";
    }
}
