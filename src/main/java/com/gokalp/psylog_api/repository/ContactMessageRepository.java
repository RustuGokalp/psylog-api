package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
