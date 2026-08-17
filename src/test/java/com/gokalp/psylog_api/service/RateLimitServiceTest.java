package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for the in-memory per-IP rate limiter.
class RateLimitServiceTest {

    private RateLimitService service(int contactPerHour, int contactPerDay,
                                     int commentPerHour, int commentPerDay) {
        return new RateLimitService(contactPerHour, contactPerDay, commentPerHour, commentPerDay);
    }

    @Test
    void allowsRequestsUpToTheHourlyLimit() {
        RateLimitService service = service(3, 10, 5, 20);

        assertDoesNotThrow(() -> {
            service.checkContact("1.1.1.1");
            service.checkContact("1.1.1.1");
            service.checkContact("1.1.1.1");
        });
    }

    @Test
    void rejectsTheRequestAfterTheHourlyLimit() {
        RateLimitService service = service(3, 10, 5, 20);
        for (int i = 0; i < 3; i++) {
            service.checkContact("2.2.2.2");
        }

        RateLimitExceededException ex =
                assertThrows(RateLimitExceededException.class, () -> service.checkContact("2.2.2.2"));
        assertEquals(RateLimitService.LIMIT_MESSAGE, ex.getMessage());
    }

    @Test
    void rejectsWhenDailyLimitIsLowerThanHourlyLimit() {
        RateLimitService service = service(10, 2, 5, 20);
        service.checkContact("3.3.3.3");
        service.checkContact("3.3.3.3");

        assertThrows(RateLimitExceededException.class, () -> service.checkContact("3.3.3.3"));
    }

    @Test
    void differentIpsDoNotShareABucket() {
        RateLimitService service = service(1, 10, 5, 20);
        service.checkContact("4.4.4.4");

        assertThrows(RateLimitExceededException.class, () -> service.checkContact("4.4.4.4"));
        assertDoesNotThrow(() -> service.checkContact("5.5.5.5"));
    }

    @Test
    void contactAndCommentBucketsAreIndependent() {
        RateLimitService service = service(1, 10, 5, 20);
        service.checkContact("6.6.6.6");

        assertThrows(RateLimitExceededException.class, () -> service.checkContact("6.6.6.6"));
        assertDoesNotThrow(() -> service.checkComment("6.6.6.6"));
    }

    @Test
    void commentEndpointUsesItsOwnLimit() {
        RateLimitService service = service(3, 10, 2, 20);
        service.checkComment("7.7.7.7");
        service.checkComment("7.7.7.7");

        assertThrows(RateLimitExceededException.class, () -> service.checkComment("7.7.7.7"));
    }

    @Test
    void evictionKeepsActiveBucketsIntact() {
        RateLimitService service = service(2, 10, 5, 20);
        service.checkContact("8.8.8.8");

        service.evictStaleBuckets();

        // The bucket was used just now, so its counter must survive the cleanup run.
        service.checkContact("8.8.8.8");
        assertThrows(RateLimitExceededException.class, () -> service.checkContact("8.8.8.8"));
    }
}
