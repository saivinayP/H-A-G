package com.hag.core.data;

import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility for generating dynamic test data.
 */
public final class DataGenerator {

    private static final AtomicInteger SEQUENCE_ID = new AtomicInteger(1);

    private DataGenerator() {}

    public static String generate(String functionToken) {
        String upperToken = functionToken.toUpperCase();
        
        // Personal
        if (upperToken.startsWith("RANDOM_EMAIL")) return generateEmail(upperToken);
        if (upperToken.equals("RANDOM_FIRST_NAME")) return randomFrom(DataGeneratorData.FIRST_NAMES);
        if (upperToken.equals("RANDOM_LAST_NAME")) return randomFrom(DataGeneratorData.LAST_NAMES);
        if (upperToken.equals("RANDOM_FULL_NAME")) return randomFrom(DataGeneratorData.FIRST_NAMES) + " " + randomFrom(DataGeneratorData.LAST_NAMES);
        if (upperToken.equals("RANDOM_USERNAME")) return RandomStringUtils.randomAlphanumeric(10).toLowerCase();
        if (upperToken.startsWith("RANDOM_PASSWORD")) return generatePassword(upperToken);
        if (upperToken.equals("RANDOM_PHONE")) return "+1-" + generateNumber(3) + "-" + generateNumber(4) + "-" + generateNumber(4);
        if (upperToken.equals("RANDOM_PHONE_IN")) return "+91-" + generateNumber(10);
        if (upperToken.equals("RANDOM_DOB")) return generateDateMinusDays(365 * 18 + ThreadLocalRandom.current().nextInt(365 * 40));
        if (upperToken.equals("RANDOM_GENDER")) return randomFrom(DataGeneratorData.GENDERS);

        // Geographic
        if (upperToken.equals("RANDOM_CITY")) return randomFrom(DataGeneratorData.CITIES);
        if (upperToken.equals("RANDOM_STATE")) return randomFrom(DataGeneratorData.STATES);
        if (upperToken.equals("RANDOM_COUNTRY")) return randomFrom(DataGeneratorData.COUNTRIES);
        if (upperToken.equals("RANDOM_COUNTRY_CODE")) return randomFrom(DataGeneratorData.COUNTRY_CODES);
        if (upperToken.equals("RANDOM_ZIP")) return generateNumber(5);
        if (upperToken.equals("RANDOM_ADDRESS")) return generateNumber(3) + " " + randomFrom(DataGeneratorData.STREET_NAMES);

        // Identifiers
        if (upperToken.equals("RANDOM_UUID") || upperToken.equals("UUID")) return UUID.randomUUID().toString();
        if (upperToken.startsWith("RANDOM_ID:")) return upperToken.substring(10) + RandomStringUtils.randomAlphanumeric(6);
        if (upperToken.startsWith("RANDOM_ALPHANUMERIC")) return generateLengthString(upperToken, true, true);
        if (upperToken.startsWith("RANDOM_ALPHA")) return generateLengthString(upperToken, true, false);
        if (upperToken.startsWith("RANDOM_NUMERIC")) return generateLengthString(upperToken, false, true);
        if (upperToken.equals("SEQ_ID")) return String.valueOf(SEQUENCE_ID.getAndIncrement());

        // Numbers
        if (upperToken.startsWith("RANDOM_NUMBER")) return generateLengthString(upperToken, false, true);
        if (upperToken.startsWith("RANDOM_INTEGER")) return generateIntegerRange(upperToken);
        if (upperToken.equals("RANDOM_PRICE")) return String.format("%.2f", ThreadLocalRandom.current().nextDouble(5.0, 500.0));
        if (upperToken.equals("RANDOM_PERCENTAGE")) return String.valueOf(ThreadLocalRandom.current().nextInt(0, 101));
        if (upperToken.startsWith("RANDOM_DECIMAL")) return generateDecimal(upperToken);

        // Dates & Times
        if (upperToken.equals("TODAY") || upperToken.equals("CURRENT_DATE")) return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        if (upperToken.equals("DATETIME") || upperToken.equals("CURRENT_DATETIME")) return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (upperToken.equals("TIMESTAMP") || upperToken.equals("CURRENT_TIMESTAMP")) return String.valueOf(System.currentTimeMillis());
        if (upperToken.startsWith("DATE_PLUS")) return generateDatePlusMinus(upperToken, true);
        if (upperToken.startsWith("DATE_MINUS")) return generateDatePlusMinus(upperToken, false);
        if (upperToken.equals("RANDOM_PAST_DATE")) return generateDateMinusDays(ThreadLocalRandom.current().nextInt(1, 365 * 2));
        if (upperToken.equals("RANDOM_FUTURE_DATE")) return generateDatePlusDays(ThreadLocalRandom.current().nextInt(1, 365 * 2));
        if (upperToken.startsWith("RANDOM_DATE")) return generateDateRange(upperToken);
        if (upperToken.equals("RANDOM_YEAR")) return String.valueOf(LocalDate.now().getYear() - ThreadLocalRandom.current().nextInt(10));

        // Finance
        if (upperToken.equals("RANDOM_CREDIT_CARD")) return "424242424242" + generateNumber(4);
        if (upperToken.equals("RANDOM_IBAN")) return "GB82WEST" + generateNumber(14);
        if (upperToken.equals("RANDOM_CURRENCY_CODE")) return randomFrom(DataGeneratorData.CURRENCY_CODES);
        if (upperToken.equals("RANDOM_AMOUNT")) return String.format("%.2f", ThreadLocalRandom.current().nextDouble(10.0, 1000.0));

        // Network & Technical
        if (upperToken.equals("RANDOM_IP") || upperToken.equals("IP_V4")) return generateIpV4();
        if (upperToken.equals("RANDOM_IPV6") || upperToken.equals("IP_V6")) return generateIpV6();
        if (upperToken.equals("RANDOM_MAC") || upperToken.equals("MAC_ADDRESS")) return generateMac();
        if (upperToken.equals("RANDOM_URL")) return "https://" + generateLengthString("RANDOM_STRING:10", true, false).toLowerCase() + ".example.com";
        if (upperToken.equals("RANDOM_DOMAIN")) return generateLengthString("RANDOM_STRING:10", true, false).toLowerCase() + ".example.com";

        // Business & Text
        if (upperToken.equals("RANDOM_COMPANY") || upperToken.equals("COMPANY_NAME")) return randomFrom(DataGeneratorData.COMPANIES);
        if (upperToken.equals("RANDOM_JOB_TITLE") || upperToken.equals("JOB_TITLE")) return randomFrom(DataGeneratorData.JOB_TITLES);
        if (upperToken.equals("RANDOM_DEPARTMENT") || upperToken.equals("INDUSTRY")) return randomFrom(DataGeneratorData.DEPARTMENTS);
        if (upperToken.equals("RANDOM_WORD") || upperToken.equals("RANDOM_STRING")) return randomFrom(DataGeneratorData.WORDS);
        if (upperToken.equals("RANDOM_SENTENCE")) return generateSentence();

        // Legacy / Generic handling
        if (upperToken.startsWith("RANDOM_STRING:")) return generateLengthString(upperToken, true, true);
        if (upperToken.startsWith("PAST_DATE:")) return generateDatePlusMinus(upperToken.replace("PAST_DATE", "DATE_MINUS"), false);
        if (upperToken.startsWith("FUTURE_DATE:")) return generateDatePlusMinus(upperToken.replace("FUTURE_DATE", "DATE_PLUS"), true);

        throw new IllegalArgumentException("Unknown data generation token: " + functionToken);
    }

    private static String randomFrom(String[] array) {
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }

    private static String generateNumber(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

    private static String generateEmail(String token) {
        String domain = "example.com";
        if (token.contains(":")) {
            domain = token.substring(token.indexOf(":") + 1).toLowerCase();
        }
        return RandomStringUtils.randomAlphanumeric(10).toLowerCase() + "@" + domain;
    }

    private static String generatePassword(String token) {
        int length = 12; // default
        if (token.contains(":")) {
            length = Integer.parseInt(token.substring(token.indexOf(":") + 1));
        }
        // Minimal constraint guarantees
        return "A1a!" + RandomStringUtils.randomAlphanumeric(Math.max(4, length - 4));
    }

    private static String generateLengthString(String token, boolean alpha, boolean numeric) {
        int length = 8;
        if (token.contains(":")) {
            length = Integer.parseInt(token.substring(token.indexOf(":") + 1));
        }
        if (alpha && numeric) return RandomStringUtils.randomAlphanumeric(length);
        if (alpha) return RandomStringUtils.randomAlphabetic(length);
        return RandomStringUtils.randomNumeric(length);
    }

    private static String generateIntegerRange(String token) {
        // format: RANDOM_INTEGER:min:max
        String[] parts = token.split(":");
        if (parts.length == 3) {
            int min = Integer.parseInt(parts[1]);
            int max = Integer.parseInt(parts[2]);
            return String.valueOf(ThreadLocalRandom.current().nextInt(min, max + 1));
        }
        return String.valueOf(ThreadLocalRandom.current().nextInt(1, 100));
    }

    private static String generateDecimal(String token) {
        int decimalPlaces = 2;
        if (token.contains(":")) {
            decimalPlaces = Integer.parseInt(token.substring(token.indexOf(":") + 1));
        }
        double value = ThreadLocalRandom.current().nextDouble(1.0, 1000.0);
        return String.format("%." + decimalPlaces + "f", value);
    }

    private static String generateDatePlusDays(int days) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static String generateDateMinusDays(int days) {
        return LocalDate.now().minusDays(days).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static String generateDatePlusMinus(String token, boolean future) {
        int days = 1;
        if (token.contains(":")) {
            days = Integer.parseInt(token.substring(token.indexOf(":") + 1));
        }
        return future ? generateDatePlusDays(days) : generateDateMinusDays(days);
    }

    private static String generateDateRange(String token) {
        // format: RANDOM_DATE:YYYY-MM-DD:YYYY-MM-DD
        String[] parts = token.split(":");
        if (parts.length == 3) {
            long minDay = LocalDate.parse(parts[1]).toEpochDay();
            long maxDay = LocalDate.parse(parts[2]).toEpochDay();
            long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay + 1);
            return LocalDate.ofEpochDay(randomDay).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static String generateIpV4() {
        return ThreadLocalRandom.current().nextInt(1, 255) + "." +
               ThreadLocalRandom.current().nextInt(0, 255) + "." +
               ThreadLocalRandom.current().nextInt(0, 255) + "." +
               ThreadLocalRandom.current().nextInt(1, 255);
    }

    private static String generateIpV6() {
        return "2001:db8:" +
               Integer.toHexString(ThreadLocalRandom.current().nextInt(0, 65535)) + ":" +
               Integer.toHexString(ThreadLocalRandom.current().nextInt(0, 65535)) + ":" +
               Integer.toHexString(ThreadLocalRandom.current().nextInt(0, 65535)) + ":" +
               Integer.toHexString(ThreadLocalRandom.current().nextInt(0, 65535)) + ":" +
               Integer.toHexString(ThreadLocalRandom.current().nextInt(0, 65535)) + ":" +
               Integer.toHexString(ThreadLocalRandom.current().nextInt(0, 65535));
    }

    private static String generateMac() {
        return String.format("00:%02x:%02x:%02x:%02x:%02x",
                ThreadLocalRandom.current().nextInt(0, 255),
                ThreadLocalRandom.current().nextInt(0, 255),
                ThreadLocalRandom.current().nextInt(0, 255),
                ThreadLocalRandom.current().nextInt(0, 255),
                ThreadLocalRandom.current().nextInt(0, 255));
    }

    private static String generateSentence() {
        StringBuilder sb = new StringBuilder();
        int words = ThreadLocalRandom.current().nextInt(4, 10);
        for (int i = 0; i < words; i++) {
            String word = randomFrom(DataGeneratorData.WORDS);
            if (i == 0) word = word.substring(0, 1).toUpperCase() + word.substring(1);
            sb.append(word).append(i < words - 1 ? " " : ".");
        }
        return sb.toString();
    }

    /**
     * Checks if a token is a known generation function.
     */
    public static boolean isGenerationFunction(String token) {
        String upper = token.toUpperCase();
        return upper.startsWith("RANDOM_") || 
               upper.startsWith("UUID") || 
               upper.startsWith("DATE_") ||
               upper.startsWith("PAST_DATE") ||
               upper.startsWith("FUTURE_DATE") ||
               upper.contains("TIMESTAMP") ||
               upper.contains("DATETIME") ||
               upper.equals("TODAY") ||
               upper.equals("SEQ_ID") ||
               upper.startsWith("IP_V") ||
               upper.equals("MAC_ADDRESS") ||
               upper.equals("COMPANY_NAME") ||
               upper.equals("JOB_TITLE") ||
               upper.equals("INDUSTRY") ||
               upper.equals("CURRENT_DATE");
    }
}
