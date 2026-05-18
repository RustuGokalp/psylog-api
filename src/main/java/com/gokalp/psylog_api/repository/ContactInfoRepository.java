package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {
    Optional<ContactInfo> findFirstBy();
}
