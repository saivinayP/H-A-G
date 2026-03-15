package com.hag.core.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataGeneratorTest {

    @Test
    public void testRandomEmail() {
        String email = DataGenerator.generate("RANDOM_EMAIL");
        Assert.assertNotNull(email);
        Assert.assertTrue(email.endsWith("@example.com"));
        Assert.assertTrue(email.contains("@"));
    }

    @Test
    public void testRandomEmailWithDomain() {
        String email = DataGenerator.generate("RANDOM_EMAIL:custom.org");
        Assert.assertNotNull(email);
        Assert.assertTrue(email.endsWith("@custom.org"));
    }

    @Test
    public void testRandomString() {
        String str = DataGenerator.generate("RANDOM_STRING");
        Assert.assertNotNull(str);
        Assert.assertEquals(str.length(), 8); // Default length
    }

    @Test
    public void testRandomStringWithLength() {
        String str = DataGenerator.generate("RANDOM_STRING:12");
        Assert.assertNotNull(str);
        Assert.assertEquals(str.length(), 12);
    }

    @Test
    public void testCurrentDate() {
        String dateStr = DataGenerator.generate("CURRENT_DATE");
        Assert.assertEquals(dateStr, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @Test
    public void testFutureDate() {
        String dateStr = DataGenerator.generate("FUTURE_DATE:5");
        Assert.assertEquals(dateStr, LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @Test
    public void testUuid() {
        String uuid = DataGenerator.generate("UUID");
        Assert.assertNotNull(uuid);
        Assert.assertEquals(uuid.length(), 36); 
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidToken() {
        DataGenerator.generate("UNKNOWN_TOKEN");
    }
}
