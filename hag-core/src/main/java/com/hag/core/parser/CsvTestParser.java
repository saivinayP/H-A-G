package com.hag.core.parser;

import com.hag.core.model.Step;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parses a CSV test file into a list of {@link Step} objects.
 *
 * <p>Uses OpenCSV so that:
 * <ul>
 *   <li>Fields containing commas can be quoted without breaking the parser</li>
 *   <li>Quoted fields containing newlines are handled correctly</li>
 *   <li>All four columns are guaranteed even when trailing columns are empty</li>
 * </ul>
 *
 * <p>Lines matched by {@link CsvLineFilter} (blank and comment lines) are
 * skipped before mapping.
 */
public class CsvTestParser {

    public List<Step> parse(final Path csvPath) {

        List<Step> steps = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(
                Files.newBufferedReader(csvPath)
        )
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build()) {

            // Validate header row
            String[] header = reader.readNext();
            if (header == null) {
                throw new IllegalArgumentException(
                        "CSV file is empty: " + csvPath
                );
            }
            CsvHeaderValidator.validate(Arrays.asList(header));

            // Data rows
            String[] raw;
            while ((raw = reader.readNext()) != null) {

                String rawLine = String.join(",", raw);

                if (CsvLineFilter.isIgnorable(rawLine)) {
                    continue;
                }

                Step step = CsvRowMapper.map(toList(raw), rawLine);
                steps.add(step);
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

        return steps;
    }

    /** Converts a raw OpenCSV string array to a list, replacing nulls with empty strings. */
    private static List<String> toList(final String[] raw) {
        List<String> list = new ArrayList<>(raw.length);
        for (String cell : raw) {
            list.add(cell == null ? "" : cell);
        }
        return list;
    }
}
