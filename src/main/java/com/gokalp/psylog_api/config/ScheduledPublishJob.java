package com.gokalp.psylog_api.config;

import com.gokalp.psylog_api.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ScheduledPublishJob {

    private static final Logger log = LoggerFactory.getLogger(ScheduledPublishJob.class);

    private final PostRepository postRepository;

    public ScheduledPublishJob(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void publishScheduledPosts() {
        int count = postRepository.publishScheduledPosts(LocalDateTime.now());
        if (count > 0) {
            log.info("Scheduled publish: {} post(s) published", count);
        }
    }
}
