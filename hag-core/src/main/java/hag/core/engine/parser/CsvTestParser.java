package hag.core.engine.parser;

import hag.core.engine.model.Step;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvTestParser {

    public List<Step> parse(Path csvPath) {
        List<Step> steps = new ArrayList<>();

        try(BufferedReader reader = Files.newBufferedReader(csvPath)) {

            String headerLine = reader.readLine();
            if(headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty: " +  csvPath);
            }

            List<String> headers = parseLine(headerLine);
            CsvHeaderValidator.validate(headers);

            String line;
            while((line = reader.readLine()) != null) {
                if(CsvLineFilter.isIgnorable(line)) {
                    continue;
                }

                List<String> values = parseLine(line);
                Step step = CsvRowMapper.map(values, line);
                steps.add(step);
            }
        } catch(IOException e) {
            throw new RuntimeException("Failed to read test file: " + csvPath, e);
        }

        return steps;
    }

    private List<String> parseLine(String line) {
        return Arrays.asList(line.split(",", -1));
    }
}
