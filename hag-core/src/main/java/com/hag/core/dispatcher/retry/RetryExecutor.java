package com.hag.core.dispatcher.retry;

import com.hag.core.result.ExecutionResult;

public final class RetryExecutor {

    private RetryExecutor() {}

    public static ExecutionResult executeWithRetry(
            int maxAttempts,
            RetryableOperation operation
    ) {

        ExecutionResult result = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try {
                result = operation.execute();
            } catch (Exception ex) {
                throw ex; // infrastructure error, do not retry
            }

            if (result.isSuccess()) {
                return result;
            }

            if (attempt < maxAttempts) {
                sleepSilently(300);
            }
        }

        return result;
    }

    private static void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    public interface RetryableOperation {
        ExecutionResult execute();
    }
}