package com.hag.core.dispatcher.retry;

import com.hag.core.result.ExecutionResult;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RetryExecutorTest {

    @Test
    public void testSuccessOnFirstAttempt() {
        ExecutionResult result = RetryExecutor.executeWithRetry(3, () ->
                ExecutionResult.success("Success")
        );
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(result.getMessage(), "Success");
    }

    @Test
    public void testRetryOnFailureAndSuccess() {
        int[] attempts = {0};
        ExecutionResult result = RetryExecutor.executeWithRetry(3, () -> {
            attempts[0]++;
            if (attempts[0] < 3) {
                return ExecutionResult.failure("Failed");
            }
            return ExecutionResult.success("Finally Success");
        });
        
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(attempts[0], 3);
    }

    @Test
    public void testMaxRetriesExceeded() {
        ExecutionResult result = RetryExecutor.executeWithRetry(3, () ->
                ExecutionResult.failure("Always Failing")
        );
        Assert.assertTrue(result.isFailure());
        Assert.assertEquals(result.getMessage(), "Always Failing");
    }

    @Test
    public void testNonRetryableExceptionFailsFast() {
        int[] attempts = {0};
        try {
            RetryExecutor.executeWithRetry(3, () -> {
                attempts[0]++;
                throw new IllegalArgumentException("Invalid argument!");
            });
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Invalid argument!");
            Assert.assertEquals(attempts[0], 1, "Should fail fast without retrying");
        }
    }

    @Test
    public void testRetryableExceptionAndSuccess() {
        int[] attempts = {0};
        ExecutionResult result = RetryExecutor.executeWithRetry(3, () -> {
            attempts[0]++;
            if (attempts[0] < 3) {
                throw new RuntimeException("Temporary network issue");
            }
            return ExecutionResult.success("Retry Success");
        });
        
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(attempts[0], 3);
    }
}
