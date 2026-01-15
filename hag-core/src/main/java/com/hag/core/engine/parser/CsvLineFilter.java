package com.hag.core.engine.parser;

public final class CsvLineFilter {
    private CsvLineFilter() {}

    public static boolean isIgnorable(String line) {
        if(line == null) return true;

        String trimmedLine = line.trim();
        return trimmedLine.isEmpty()
                || trimmedLine.startsWith("#")
                || trimmedLine.startsWith("//");
    }
}
