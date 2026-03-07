package com.hag.db.bootstrap;

import com.hag.core.dispatcher.ActionRegistry;
import com.hag.db.action.AssertColumnAction;
import com.hag.db.action.AssertRowCountAction;
import com.hag.db.action.DbExecuteAction;
import com.hag.db.action.DbQueryAction;
import com.hag.db.action.StoreDataDbAction;
import com.hag.db.adapter.JdbcDbAdapter;

/**
 * DbBootstrap
 *
 * <p>Registers all JDBC database actions into the {@link ActionRegistry}
 * and provides a factory method to create the {@link JdbcDbAdapter}.
 */
public final class DbBootstrap {

    private DbBootstrap() {}

    /**
     * Registers all DB-layer actions.
     *
     * @param registry the central action registry
     */
    public static void registerDbActions(ActionRegistry registry) {

        // ── Query ────────────────────────────────────────────────────────
        registry.register(new DbQueryAction());       // DB_QUERY / DB_QUERY:INLINE

        // ── DML ──────────────────────────────────────────────────────────
        registry.register(new DbExecuteAction());     // DB_EXECUTE / DB_EXECUTE:INLINE

        // ── Assertions ───────────────────────────────────────────────────
        registry.register(new AssertRowCountAction()); // ASSERT_ROW_COUNT / :AT_LEAST / :AT_MOST / :ZERO / :NOT_ZERO
        registry.register(new AssertColumnAction());   // ASSERT_COLUMN / :CONTAINS / :NOT_EQUALS / :NOT_NULL / :NULL

        // ── Data extraction ──────────────────────────────────────────────
        registry.register(new StoreDataDbAction());   // STORE_DATA:DB / STORE_DATA:DB_COUNT
    }

    /**
     * Creates a {@link JdbcDbAdapter} from connection parameters.
     *
     * @param jdbcUrl  JDBC connection URL (e.g. {@code jdbc:mysql://host:3306/db})
     * @param username DB username
     * @param password DB password
     * @return configured adapter ready to use
     */
    public static JdbcDbAdapter createAdapter(String jdbcUrl, String username, String password) {
        return new JdbcDbAdapter(jdbcUrl, username, password);
    }
}
