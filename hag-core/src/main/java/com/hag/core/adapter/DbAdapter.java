package com.hag.core.adapter;

import java.util.List;
import java.util.Map;

/**
 * Marker interface for database capability provider.
 */
public interface DbAdapter {

    /**
     * Executes SQL query and returns rows.
     */
    List<Map<String, Object>> executeQuery(String sql);

    /**
     * Executes update/insert/delete.
     */
    int executeUpdate(String sql);
}