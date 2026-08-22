package com.gokalp.psylog_api.config;

import com.gokalp.psylog_api.entity.PageHero;
import com.gokalp.psylog_api.entity.PageKey;
import com.gokalp.psylog_api.repository.PageHeroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// Verifies the page hero seed: it inserts the four fixed records at startup
// and never overwrites what the admin has already edited.
@SpringBootTest
class PageHeroSeedTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private PageHeroRepository pageHeroRepository;

    @Test
    void seed_createsFourRecordsWithExpectedDefaults() {
        assertThat(pageHeroRepository.count()).isEqualTo(4);

        PageHero about = pageHeroRepository.findByPageKey(PageKey.ABOUT).orElseThrow();
        assertThat(about.getSubtitle()).isEqualTo("Hakkımda");
        assertThat(about.getTitle()).isEqualTo("Klinik Psikolog Tuğçe Tekin");

        PageHero specializations = pageHeroRepository.findByPageKey(PageKey.SPECIALIZATIONS).orElseThrow();
        assertThat(specializations.getSubtitle()).isEqualTo("Uzmanlık Alanları");
        assertThat(specializations.getTitle()).isEqualTo("Çalışma Alanlarım");
    }

    @Test
    void seed_isIdempotent_doesNotOverwriteExistingRecords() {
        PageHero posts = pageHeroRepository.findByPageKey(PageKey.POSTS).orElseThrow();
        posts.setTitle("Admin tarafından düzenlendi");
        pageHeroRepository.save(posts);

        dataInitializer.run();

        assertThat(pageHeroRepository.count()).isEqualTo(4);
        assertThat(pageHeroRepository.findByPageKey(PageKey.POSTS).orElseThrow().getTitle())
                .isEqualTo("Admin tarafından düzenlendi");

        // restore so the shared context is left as seeded
        PageHero restored = pageHeroRepository.findByPageKey(PageKey.POSTS).orElseThrow();
        restored.setTitle("Yazılarım");
        pageHeroRepository.save(restored);
    }
}
