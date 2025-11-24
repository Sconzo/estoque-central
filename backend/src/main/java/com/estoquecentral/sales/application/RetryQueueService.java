package com.estoquecentral.sales.application;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * RetryQueueService - Manages NFCe retry queue with exponential backoff
 * Story 4.4: NFCe Retry Queue and Failure Management
 *
 * <p>Uses Redis delayed queue to schedule retries with exponential backoff:
 * <ul>
 *   <li>Attempt 1: 1 minute delay</li>
 *   <li>Attempt 2: 2 minutes delay</li>
 *   <li>Attempt 3: 4 minutes delay</li>
 *   <li>...</li>
 *   <li>Max delay: 8 hours</li>
 *   <li>Max attempts: 10</li>
 * </ul>
 */
@Service
public class RetryQueueService {

    private static final Logger logger = LoggerFactory.getLogger(RetryQueueService.class);

    // Constants for retry logic
    public static final int MAX_ATTEMPTS = 10;
    public static final long MAX_DELAY_SECONDS = 8 * 60 * 60; // 8 hours in seconds
    private static final long BASE_DELAY_SECONDS = 60; // 1 minute base delay

    private final RedissonClient redissonClient;
    private final RDelayedQueue<UUID> delayedQueue;
    private final RBlockingQueue<UUID> blockingQueue;

    public RetryQueueService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;

        // Initialize blocking queue and delayed queue
        this.blockingQueue = redissonClient.getBlockingQueue("nfce-retry-queue");
        this.delayedQueue = redissonClient.getDelayedQueue(blockingQueue);

        logger.info("RetryQueueService initialized with MAX_ATTEMPTS={}, MAX_DELAY={}s",
                MAX_ATTEMPTS, MAX_DELAY_SECONDS);
    }

    /**
     * Enqueue sale for retry with exponential backoff delay
     * Formula: min(2^attempt * 60 seconds, 8 hours)
     *
     * @param saleId sale ID to retry
     * @param attemptCount current attempt count (0-based)
     */
    public void enqueue(UUID saleId, int attemptCount) {
        long delaySeconds = calculateDelay(attemptCount);

        // Add to delayed queue with calculated delay
        delayedQueue.offer(saleId, delaySeconds, TimeUnit.SECONDS);

        logger.info("Sale {} enqueued for retry. Attempt: {}, Delay: {}s",
                saleId, attemptCount + 1, delaySeconds);
    }

    /**
     * Dequeue next sale ID from retry queue (blocking with timeout)
     * Returns null if no items available within timeout
     *
     * @return next sale ID to retry, or null if queue is empty
     */
    public UUID dequeue() {
        try {
            // Poll with 5 second timeout to avoid blocking indefinitely
            return blockingQueue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Dequeue interrupted: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Calculate exponential backoff delay
     * Formula: min(2^attempt * 60 seconds, 8 hours)
     *
     * @param attemptCount current attempt count (0-based)
     * @return delay in seconds
     */
    private long calculateDelay(int attemptCount) {
        // Calculate exponential delay: 2^attempt * 60 seconds
        long exponentialDelay = (long) Math.pow(2, attemptCount) * BASE_DELAY_SECONDS;

        // Cap at MAX_DELAY_SECONDS (8 hours)
        long delaySeconds = Math.min(exponentialDelay, MAX_DELAY_SECONDS);

        return delaySeconds;
    }

    /**
     * Get current queue size (for monitoring)
     *
     * @return number of items in queue
     */
    public int getQueueSize() {
        return blockingQueue.size();
    }

    /**
     * Remove sale from queue (if manual resolution occurs)
     *
     * @param saleId sale ID to remove
     * @return true if removed, false if not found
     */
    public boolean remove(UUID saleId) {
        boolean removed = blockingQueue.remove(saleId);
        if (removed) {
            logger.info("Sale {} removed from retry queue", saleId);
        }
        return removed;
    }
}
