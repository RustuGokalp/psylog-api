package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    @Query("SELECT s FROM Specialization s ORDER BY s.displayOrder ASC NULLS LAST, s.createdAt ASC")
    List<Specialization> findAllOrdered();

    Optional<Specialization> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
