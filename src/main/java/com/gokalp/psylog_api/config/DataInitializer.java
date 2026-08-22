package com.gokalp.psylog_api.config;

import com.gokalp.psylog_api.entity.PageHero;
import com.gokalp.psylog_api.entity.PageKey;
import com.gokalp.psylog_api.entity.Role;
import com.gokalp.psylog_api.entity.User;
import com.gokalp.psylog_api.repository.PageHeroRepository;
import com.gokalp.psylog_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // Seed values for the page hero strips. Only missing pageKeys are inserted —
    // existing records are never overwritten, so admin edits survive a redeploy.
    private static final List<PageHeroSeed> PAGE_HERO_SEEDS = List.of(
            new PageHeroSeed(
                    PageKey.ABOUT,
                    "Hakkımda",
                    "Klinik Psikolog Tuğçe Tekin",
                    "Çocuğunuzun iç dünyasını anlamak ve birlikte büyümek için buradayım. Güvenli, sıcak ve destekleyici bir ortamda yanınızdayım."),
            new PageHeroSeed(
                    PageKey.POSTS,
                    "Blog",
                    "Yazılarım",
                    "Çocuk ve ergen psikolojisi üzerine yazılar."),
            new PageHeroSeed(
                    PageKey.CONTACT,
                    "Randevu ve Bilgi",
                    "İletişime Geçin",
                    "Çocuğunuz veya ergeniniz için profesyonel destek almak istiyorsanız, aşağıdaki formu doldurabilir veya doğrudan iletişim bilgilerimi kullanabilirsiniz."),
            new PageHeroSeed(
                    PageKey.SPECIALIZATIONS,
                    "Uzmanlık Alanları",
                    "Çalışma Alanlarım",
                    "Danışanlarımla yürüttüğüm terapötik çalışmalarda odaklandığım başlıca uzmanlık alanlarını aşağıda bulabilirsiniz.")
    );

    private final UserRepository userRepository;
    private final PageHeroRepository pageHeroRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository,
                           PageHeroRepository pageHeroRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.pageHeroRepository = pageHeroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedPageHeroes();
    }

    private void seedAdminUser() {
        if (userRepository.count() == 0) {
            User admin = new User(adminEmail, passwordEncoder.encode(adminPassword), Role.ADMIN);
            userRepository.save(admin);
            log.info("Admin user created: {}", adminEmail);
        }
    }

    private void seedPageHeroes() {
        for (PageHeroSeed seed : PAGE_HERO_SEEDS) {
            if (!pageHeroRepository.existsByPageKey(seed.pageKey())) {
                pageHeroRepository.save(new PageHero(seed.pageKey(), seed.subtitle(), seed.title(), seed.description()));
                log.info("Page hero seeded: {}", seed.pageKey());
            }
        }
    }

    private record PageHeroSeed(PageKey pageKey, String subtitle, String title, String description) {}
}
