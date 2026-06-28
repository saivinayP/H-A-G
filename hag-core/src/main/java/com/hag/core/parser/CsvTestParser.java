package com.hag.core.parser;

import com.hag.core.model.Step;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parses a CSV test file into a {@link ParseResult} containing:
 * <ul>
 *   <li>An executable {@link Step} list</li>
 *   <li>A {@link TestMetadata} object built from {@code #!} directives</li>
 * </ul>
 *
 * <h3>File structure</h3>
 * <pre>
 *   #!NAME: My Test               ← test-level directives (before header row)
 *   #!TAGS: smoke, regression
 *   Action,Recipient,Source,Key   ← header row (required exactly once)
 *   # plain comment               ← ignored
 *   #!SECTION: Login Phase        ← section directive → produces SECTION step
 *   NAVIGATE,,,/login             ← normal data row
 *   ...
 * </pre>
 *
 * <p>Uses OpenCSV so that:
 * <ul>
 *   <li>Fields containing commas can be quoted without breaking the parser</li>
 *   <li>Quoted fields containing newlines are handled correctly</li>
 *   <li>All four columns are guaranteed even when trailing columns are empty</li>
 * </ul>
 */
public class CsvTestParser {

    /**
     * Parses the CSV file and returns both the step list and test metadata.
     *
     * @param csvPath path to the CSV file
     * @return a {@link ParseResult} — never {@code null}
     */
    public ParseResult parse(final Path csvPath) {

        TestMetadata    metadata = TestMetadata.empty();
        List<Step>      steps   = new ArrayList<>();

        try {
            // ── Pre-scan: read #! directives that appear BEFORE the header row ──
            // We need a plain reader for this because OpenCSV will consume the stream.
            List<String> preambleDirectives = readPreambleDirectives(csvPath);
            for (String line : preambleDirectives) {
                metadata.apply(Directive.parse(line));
            }

            // ── Main parse via OpenCSV ────────────────────────────────────────
            try (CSVReader reader = new CSVReaderBuilder(
                    Files.newBufferedReader(csvPath)
            )
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    .build()) {

                // Skip preamble directive lines and find the header row
                String[] headerRaw = null;
                String[] raw;
                while ((raw = reader.readNext()) != null) {
                    String joined = String.join(",", replaceNulls(raw));
                    if (CsvLineFilter.isDirective(joined)) {
                        // These were already processed above; skip
                        continue;
                    }
                    if (CsvLineFilter.isIgnorable(joined)) {
                        continue;
                    }
                    // First non-ignorable, non-directive line must be the header
                    headerRaw = raw;
                    break;
                }

                if (headerRaw == null) {
                    throw new IllegalArgumentException(
                            "CSV file has no header row: " + csvPath
                    );
                }
                CsvHeaderValidator.validate(Arrays.asList(replaceNulls(headerRaw)));

                // ── Data rows ────────────────────────────────────────────
                while ((raw = reader.readNext()) != null) {
                    String[] cols    = replaceNulls(raw);
                    String   rawLine = String.join(",", cols);

                    if (CsvLineFilter.isDirective(rawLine)) {
                        // Inline directive — could be #!SECTION or mid-test #!VAR etc.
                        Directive d = Directive.parse(rawLine);
                        if ("SECTION".equals(d.key())) {
                            // Convert to a SECTION step so the report engine renders it
                            steps.add(new Step("SECTION", null, null, d.value(), rawLine));
                        } else {
                            // All other inline directives still update metadata
                            metadata.apply(d);
                        }
                        continue;
                    }

                    if (CsvLineFilter.isIgnorable(rawLine)) {
                        continue;
                    }

                    Step step = CsvRowMapper.map(List.of(cols), rawLine);
                    steps.add(step);
                }
            }

        } catch (IOException ex) {
            throw new RuntimeException(
                    "Failed to read test file: " + csvPath, ex
            );
        } catch (CsvValidationException ex) {
            throw new RuntimeException(
                    "Malformed CSV in test file: " + csvPath, ex
            );
        }

        return new ParseResult(steps, metadata);
    }

    /**
     * Legacy convenience method — returns only the step list.
     * Prefer {@link #parse(Path)} for new code.
     */
    public List<Step> parseSteps(final Path csvPath) {
        return parse(csvPath).steps();
    }

    /**
     * Reads raw text lines from the top of the file until the first non-directive,
     * non-blank, non-comment line is reached (that line is the header row and is
     * not consumed here). Returns only the {@code #!} directive lines.
     */
    private static List<String> readPreambleDirectives(final Path csvPath) throws IOException {
        List<String> directives = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String t = line.trim();
                if (t.isEmpty()) continue;
                if (CsvLineFilter.isDirective(t)) {
                    directives.add(t);
                } else if (CsvLineFilter.isComment(t)) {
                    // plain comment in preamble — skip
                } else {
                    // First non-directive, non-comment line — stop (it's the header)
                    break;
                }
            }
        }
        return directives;
    }

    /** Replaces null elements in an OpenCSV array with empty strings. */
    private static String[] replaceNulls(final String[] raw) {
        String[] result = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            result[i] = raw[i] == null ? "" : raw[i];
        }
        return result;
    }
}
