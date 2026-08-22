package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.PageHero;
import com.gokalp.psylog_api.entity.PageKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageHeroRepository extends JpaRepository<PageHero, Long> {

    List<PageHero> findAllByOrderByIdAsc();

    Optional<PageHero> findByPageKey(PageKey pageKey);

    boolean existsByPageKey(PageKey pageKey);
}
