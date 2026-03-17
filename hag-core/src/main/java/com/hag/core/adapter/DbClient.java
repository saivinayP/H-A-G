package com.hag.core.adapter;

import com.hag.core.db.DbQueryResult;

/**
 * DbClient — the primary database capability interface for the H-A-G framework.
 *
 * <p>Extends the minimal {@link DbAdapter} marker interface with the result-accessor
 * and lifecycle methods that DB actions need, eliminating the need for them to cast
 * to {@code JdbcDbAdapter} (or any concrete implementation) directly.
 *
 * <h3>Implementations</h3>
 * <ul>
 *   <li>{@code JdbcSqlClient} — JDBC/SQL backend (MySQL, MS SQL, H2, etc.)</li>
 *   <li>Future: {@code MongoDbClient} — document store backend</li>
 * </ul>
 */
public interface DbClient extends DbAdapter {

    /**
     * Returns the cached result of the most recent {@code DB_QUERY} call.
     *
     * @throws IllegalStateException if no query has been executed yet
     */
    DbQueryResult getLastQueryResult();

    /**
     * Returns the number of rows affected by the most recent {@code DB_EXECUTE} call,
     * or {@code -1} if no execute has been called yet.
     */
    int getLastAffectedRows();

    /**
     * Releases any underlying resources (connections, sockets, etc.).
     * Safe to call multiple times.
     */
    void close();
}
