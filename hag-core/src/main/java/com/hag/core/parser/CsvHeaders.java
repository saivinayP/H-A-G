package com.hag.core.parser;

import java.util.List;

public final class CsvHeaders {
    private CsvHeaders() {}

    public static final List<String> REQUIRED_HEADERS =
            List.of("Action", "Recipient", "Source", "Key");
}
