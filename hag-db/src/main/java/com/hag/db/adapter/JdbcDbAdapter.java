package com.hag.db.adapter;

import com.hag.core.adapter.DbClient;

/**
 * JdbcDbAdapter — backward-compatibility shim.
 *
 * <p>This class extends {@link JdbcSqlClient} (which now implements {@link DbClient})
 * so existing code that references {@code JdbcDbAdapter} by name continues to compile
 * and function without changes.
 *
 * @deprecated Use {@link JdbcSqlClient} directly. This class will be removed in a future release.
 */
@Deprecated(since = "1.1", forRemoval = false)
public final class JdbcDbAdapter extends JdbcSqlClient {

    public JdbcDbAdapter(String url, String username, String password) {
        super(url, username, password);
    }
}
