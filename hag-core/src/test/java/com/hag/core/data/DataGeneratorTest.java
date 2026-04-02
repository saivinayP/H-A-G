package com.hag.core.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class DataGeneratorTest {

    @Test
    public void testPersonal() {
        Assert.assertTrue(DataGenerator.generate("RANDOM_EMAIL").contains("@example.com"));
        Assert.assertTrue(DataGenerator.generate("RANDOM_EMAIL:custom.org").contains("@custom.org"));
        Assert.assertNotNull(DataGenerator.generate("RANDOM_FIRST_NAME"));
        Assert.assertNotNull(DataGenerator.generate("RANDOM_LAST_NAME"));
        Assert.assertTrue(DataGenerator.generate("RANDOM_FULL_NAME").contains(" "));
    }

    @Test
    public void testStringsAndNumbers() {
        String alpha = DataGenerator.generate("RANDOM_ALPHA:10");
        Assert.assertEquals(alpha.length(), 10);
        Assert.assertTrue(alpha.matches("[a-zA-Z]+"));

        String num = DataGenerator.generate("RANDOM_NUMERIC:5");
        Assert.assertEquals(num.length(), 5);
        Assert.assertTrue(num.matches("\\d+"));

        String range = DataGenerator.generate("RANDOM_INTEGER:10:20");
        int r = Integer.parseInt(range);
        Assert.assertTrue(r >= 10 && r <= 20);
    }

    @Test
    public void testDates() {
        String today = DataGenerator.generate("TODAY");
        Assert.assertEquals(today, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        String past = DataGenerator.generate("DATE_MINUS:5");
        Assert.assertEquals(past, LocalDate.now().minusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE));

        String future = DataGenerator.generate("DATE_PLUS:10");
        Assert.assertEquals(future, LocalDate.now().plusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @Test
    public void testIdentifiers() {
        String uuid = DataGenerator.generate("UUID");
        Assert.assertEquals(uuid.length(), 36);

        String id = DataGenerator.generate("RANDOM_ID:ORD-");
        Assert.assertTrue(id.startsWith("ORD-"));
        Assert.assertEquals(id.length(), 10);

        String seq1 = DataGenerator.generate("SEQ_ID");
        String seq2 = DataGenerator.generate("SEQ_ID");
        Assert.assertNotEquals(seq1, seq2);
    }

    @Test
    public void testNetwork() {
        String ipv4 = DataGenerator.generate("IP_V4");
        Assert.assertTrue(ipv4.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"));

        String url = DataGenerator.generate("RANDOM_URL");
        Assert.assertTrue(url.startsWith("https://"));
        Assert.assertTrue(url.endsWith(".example.com"));
    }

    @Test
    public void testFinance() {
        String cc = DataGenerator.generate("RANDOM_CREDIT_CARD");
        Assert.assertTrue(cc.startsWith("4242"));
        Assert.assertEquals(cc.length(), 16);
    }

    @Test
    public void testBusiness() {
        Assert.assertNotNull(DataGenerator.generate("RANDOM_COMPANY"));
        Assert.assertTrue(DataGenerator.generate("RANDOM_SENTENCE").endsWith("."));
    }

    @Test
    public void testLegacySupport() {
        String str = DataGenerator.generate("RANDOM_STRING:12");
        Assert.assertEquals(str.length(), 12);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidToken() {
        DataGenerator.generate("UNKNOWN_TOKEN");
    }
}
