package com.hag.core.engine.adapter;

import java.util.List;
import java.util.Map;

public interface DbAdapter {

    /**
     * Executes a SQL query and returns rows.
     * Each row is  a map: columnName -> value
     */
    List<Map<String, Object>> executeQuery(String sql);
}
