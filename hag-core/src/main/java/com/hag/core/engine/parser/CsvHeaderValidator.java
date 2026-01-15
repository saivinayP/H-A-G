package com.hag.core.engine.parser;

import java.util.List;

public final class CsvHeaderValidator {
    private CsvHeaderValidator() {
    }

    public static void validate(List<String> headers) {
        if (!headers.equals(CsvHeaders.REQUIRED_HEADERS)) {
           throw new IllegalArgumentException("Invalid CSV headers. Expected: "
                   + CsvHeaders.REQUIRED_HEADERS
                   + ", but found: " + headers);
        }
    }
}
