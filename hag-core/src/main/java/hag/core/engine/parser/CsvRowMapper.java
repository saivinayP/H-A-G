package hag.core.engine.parser;

import hag.core.engine.model.Step;

import java.util.List;

public final class CsvRowMapper {
    private CsvRowMapper() {
    }

    public static Step map(List<String> rowValues, String rawLine) {
        String action = getValue(rowValues, 0);
        String recipient = getValue(rowValues, 1);
        String source = getValue(rowValues, 2);
        String key = getValue(rowValues, 3);

        return new Step(action, recipient, source, key, rawLine);
    }

    private static String getValue(List<String> row, int index) {
        if(index >= row.size()) return null;

        String value = row.get(index);
        return value == null || value.isBlank() ? null : value.trim();
    }
}
