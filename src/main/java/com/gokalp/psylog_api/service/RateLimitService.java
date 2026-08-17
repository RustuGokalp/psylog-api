package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, per-IP fixed window rate limiter for the public endpoints that
 * trigger a notification email. Single server, so no external store is needed.
 */
@Service
public class RateLimitService {

    public static final String LIMIT_MESSAGE =
            "Çok fazla istek gönderdiniz. Lütfen bir süre sonra tekrar deneyin.";

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    private static final Duration HOUR = Duration.ofHours(1);
    private static final Duration DAY = Duration.ofDays(1);

    private final Map<String, Counters> buckets = new ConcurrentHashMap<>();

    private final int contactPerHour;
    private final int contactPerDay;
    private final int commentPerHour;
    private final int commentPerDay;

    public RateLimitService(
            @Value("${app.rate-limit.contact.per-hour:3}") int contactPerHour,
            @Value("${app.rate-limit.contact.per-day:10}") int contactPerDay,
            @Value("${app.rate-limit.comment.per-hour:5}") int commentPerHour,
            @Value("${app.rate-limit.comment.per-day:20}") int commentPerDay) {
        this.contactPerHour = contactPerHour;
        this.contactPerDay = contactPerDay;
        this.commentPerHour = commentPerHour;
        this.commentPerDay = commentPerDay;
    }

    public void checkContact(String ip) {
        check("contact", ip, contactPerHour, contactPerDay);
    }

    public void checkComment(String ip) {
        check("comment", ip, commentPerHour, commentPerDay);
    }

    private void check(String bucket, String ip, int perHour, int perDay) {
        Counters counters = buckets.computeIfAbsent(bucket + "|" + ip, key -> new Counters());
        synchronized (counters) {
            Instant now = Instant.now();
            counters.rollOver(now);
            if (counters.hourCount >= perHour || counters.dayCount >= perDay) {
                log.warn("Hız sınırı aşıldı [bucket={}, ip={}, saatlik={}/{}, günlük={}/{}]",
                        bucket, ip, counters.hourCount, perHour, counters.dayCount, perDay);
                throw new RateLimitExceededException(LIMIT_MESSAGE);
            }
            counters.hourCount++;
            counters.dayCount++;
            counters.lastSeen = now;
        }
    }

    /** Drops buckets that have been idle for longer than a day so the map cannot grow forever. */
    @Scheduled(fixedDelay = 3_600_000L, initialDelay = 3_600_000L)
    void evictStaleBuckets() {
        Instant cutoff = Instant.now().minus(DAY);
        int before = buckets.size();
        buckets.values().removeIf(counters -> counters.lastSeen.isBefore(cutoff));
        int removed = before - buckets.size();
        if (removed > 0) {
            log.info("Hız sınırı temizliği: {} kayıt silindi", removed);
        }
    }

    private static final class Counters {
        private Instant hourStart = Instant.EPOCH;
        private Instant dayStart = Instant.EPOCH;
        private int hourCount;
        private int dayCount;
        private volatile Instant lastSeen = Instant.EPOCH;

        private void rollOver(Instant now) {
            if (Duration.between(hourStart, now).compareTo(HOUR) >= 0) {
                hourStart = now;
                hourCount = 0;
            }
            if (Duration.between(dayStart, now).compareTo(DAY) >= 0) {
                dayStart = now;
                dayCount = 0;
            }
        }
    }
}
