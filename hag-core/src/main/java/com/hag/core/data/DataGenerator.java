package com.hag.core.data;

import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility for generating dynamic test data.
 * Used to resolve tokens like ${RANDOM_EMAIL}, ${RANDOM_STRING:10}, ${PAST_DATE:7}
 */
public final class DataGenerator {

    private DataGenerator() {}

    public static String generate(String functionToken) {
        String upperToken = functionToken.toUpperCase();
        
        if (upperToken.startsWith("RANDOM_EMAIL")) {
            return generateRandomEmail(upperToken);
        }
        
        if (upperToken.startsWith("RANDOM_STRING")) {
            return generateRandomString(upperToken);
        }
        
        if (upperToken.startsWith("RANDOM_NUMBER")) {
            return generateRandomNumber(upperToken);
        }
        
        if (upperToken.startsWith("UUID")) {
            return UUID.randomUUID().toString();
        }
        
        if (upperToken.startsWith("PAST_DATE")) {
            return generateDate(upperToken, false);
        }
        
        if (upperToken.startsWith("FUTURE_DATE")) {
            return generateDate(upperToken, true);
        }
        
        if (upperToken.equals("CURRENT_DATE")) {
            return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        throw new IllegalArgumentException("Unknown data generation token: " + functionToken);
    }

    private static String generateRandomEmail(String token) {
        // e.g. RANDOM_EMAIL or RANDOM_EMAIL:hag.com
        String domain = "example.com";
        if (token.contains(":")) {
            domain = token.substring(token.indexOf(":") + 1).toLowerCase();
        }
        String prefix = RandomStringUtils.randomAlphanumeric(10).toLowerCase();
        return prefix + "@" + domain;
    }

    private static String generateRandomString(String token) {
        // e.g. RANDOM_STRING or RANDOM_STRING:15
        int length = 8;
        if (token.contains(":")) {
            length = Integer.parseInt(token.substring(token.indexOf(":") + 1));
        }
        return RandomStringUtils.randomAlphabetic(length);
    }

    private static String generateRandomNumber(String token) {
        // e.g. RANDOM_NUMBER or RANDOM_NUMBER:5
        int length = 5;
        if (token.contains(":")) {
            length = Integer.parseInt(token.substring(token.indexOf(":") + 1));
        }
        return RandomStringUtils.randomNumeric(length);
    }
    
    private static String generateDate(String token, boolean future) {
        // e.g. PAST_DATE:7 or FUTURE_DATE:30
        int days = 1;
        if (token.contains(":")) {
            days = Integer.parseInt(token.substring(token.indexOf(":") + 1));
        }
        LocalDate date = LocalDate.now();
        date = future ? date.plusDays(days) : date.minusDays(days);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    /**
     * Checks if a token is a known generation function (e.g. RANDOM_*, UUID, etc.)
     */
    public static boolean isGenerationFunction(String token) {
        String upper = token.toUpperCase();
        return upper.startsWith("RANDOM_") || 
               upper.startsWith("UUID") || 
               upper.contains("_DATE");
    }
}
