package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.About;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AboutRepository extends JpaRepository<About, Long> {
    Optional<About> findFirstBy();
}
