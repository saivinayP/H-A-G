package com.hag.core.db;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable result of a SQL SELECT query.
 *
 * <p>Moved to {@code hag-core} (Phase 4) so that {@link com.hag.core.adapter.DbClient}
 * can reference it without creating a circular dependency on {@code hag-db}.
 *
 * <p>Each row is a {@code Map<String, Object>} where the key is the column name
 * (in the case returned by the JDBC driver) and the value is the raw Java object
 * from {@link java.sql.ResultSet#getObject(String)}.
 */
public final class DbQueryResult {

    private final List<Map<String, Object>> rows;
    private final List<String>              columnNames;

    public DbQueryResult(List<Map<String, Object>> rows, List<String> columnNames) {
        this.rows        = rows        != null ? Collections.unmodifiableList(rows)        : Collections.emptyList();
        this.columnNames = columnNames != null ? Collections.unmodifiableList(columnNames) : Collections.emptyList();
    }

    /** @return all rows, never {@code null} */
    public List<Map<String, Object>> rows() { return rows; }

    /** @return ordered column names from the ResultSet metadata */
    public List<String> columnNames() { return columnNames; }

    /** @return number of rows returned */
    public int rowCount() { return rows.size(); }

    /** @return {@code true} when the result set is empty */
    public boolean isEmpty() { return rows.isEmpty(); }

    /**
     * Gets the value of a column from the first row.
     *
     * @param column column name (case-insensitive)
     * @return value or {@code null} if not found
     */
    public Object firstRowValue(String column) {
        if (rows.isEmpty()) return null;
        return rows.get(0).entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(column))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the value of a column from a specific row (0-indexed).
     *
     * @param rowIndex 0-based row index
     * @param column   column name (case-insensitive)
     * @return value or {@code null}
     */
    public Object getValue(int rowIndex, String column) {
        if (rowIndex < 0 || rowIndex >= rows.size()) return null;
        return rows.get(rowIndex).entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(column))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "DbQueryResult{rows=" + rows.size() + ", columns=" + columnNames + "}";
    }
}
