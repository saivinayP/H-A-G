package com.hag.core.dispatcher.retry;

import com.hag.core.result.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retries an action up to {@code maxAttempts} times.
 *
 * <h3>Retry conditions</h3>
 * <ul>
 *   <li>Retries on {@link ExecutionResult#isFailure()} (assertion / logic failure)</li>
 *   <li>Retries on any thrown {@link Exception}
 *       (e.g. {@code StaleElementReferenceException})</li>
 *   <li>Does NOT retry on {@link Error} subclasses (JVM errors)</li>
 *   <li>Short-circuits immediately on {@link ExecutionResult#isSkipped()}</li>
 * </ul>
 *
 * <p>A 300 ms pause is inserted between attempts.
 */
public final class RetryExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(RetryExecutor.class);

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
                if (attempt >= maxAttempts) {
                    // Final attempt — propagate
                    throw (ex instanceof RuntimeException rte) ? rte
                            : new RuntimeException("Action threw on attempt " + attempt, ex);
                }
                LOG.warn("HAG → Retry {}/{} after exception: {}",
                        attempt, maxAttempts, ex.getMessage());
                sleepSilently(300);
                continue;
            }

            // Short-circuit on skipped or success
            if (result.isSuccess() || result.isSkipped()) {
                return result;
            }

            if (attempt < maxAttempts) {
                LOG.debug("HAG → Retry {}/{} after failure: {}",
                        attempt, maxAttempts, result.getMessage());
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