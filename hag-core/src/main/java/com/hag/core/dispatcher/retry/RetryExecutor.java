package com.hag.core.dispatcher.retry;

import com.hag.core.result.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retries an action up to {@code maxAttempts} times using an Exponential Backoff strategy.
 *
 * <h3>Retry conditions</h3>
 * <ul>
 *   <li>Retries on {@link ExecutionResult#isFailure()} (assertion / logic failure)</li>
 *   <li>Retries on thrown {@link Exception} (e.g., StaleElementReferenceException)
 *       unless it is a non-retryable exception like IllegalArgumentException.</li>
 *   <li>Does NOT retry on {@link Error} subclasses (JVM errors)</li>
 *   <li>Short-circuits immediately on {@link ExecutionResult#isSkipped()}</li>
 * </ul>
 *
 * <p>Sleep duration doubles after each attempt, up to a maximum cap.
 */
public final class RetryExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(RetryExecutor.class);

    private static final long INITIAL_BACKOFF_MS = 300;
    private static final long MAX_BACKOFF_MS = 3000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private RetryExecutor() {}

    public static ExecutionResult executeWithRetry(
            int maxAttempts,
            RetryableOperation operation
    ) {
        ExecutionResult result = null;
        long currentBackoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try {
                result = operation.execute();
            } catch (Exception ex) {
                if (attempt >= maxAttempts || !shouldRetry(ex)) {
                    // Final attempt or non-retryable exception — propagate
                    throw (ex instanceof RuntimeException rte) ? rte
                            : new RuntimeException("Action threw on attempt " + attempt, ex);
                }
                LOG.warn("HAG → Retry {}/{} after exception: {}",
                        attempt, maxAttempts, ex.getMessage());
                
                sleepSilently(currentBackoffMs);
                currentBackoffMs = Math.min((long) (currentBackoffMs * BACKOFF_MULTIPLIER), MAX_BACKOFF_MS);
                continue;
            }

            // Short-circuit on skipped or success
            if (result.isSuccess() || result.isSkipped()) {
                return result;
            }

            if (attempt < maxAttempts) {
                LOG.debug("HAG → Retry {}/{} after failure: {}",
                        attempt, maxAttempts, result.getMessage());
                
                sleepSilently(currentBackoffMs);
                currentBackoffMs = Math.min((long) (currentBackoffMs * BACKOFF_MULTIPLIER), MAX_BACKOFF_MS);
            }
        }

        return result;
    }

    /**
     * Categorizes exceptions to decide if a retry is worthwhile.
     */
    private static boolean shouldRetry(Exception ex) {
        // Fail-fast on programming errors and fatal framework states
        if (ex instanceof IllegalArgumentException ||
            ex instanceof IllegalStateException ||
            ex instanceof NullPointerException ||
            ex instanceof UnsupportedOperationException ||
            ex instanceof ClassCastException) {
            
            LOG.warn("HAG → Non-retryable exception encountered: {}. Failing immediately.", ex.getClass().getSimpleName());
            return false;
        }
        
        // Everything else (network timeouts, stale elements, general UI flakiness) is retryable
        return true;
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