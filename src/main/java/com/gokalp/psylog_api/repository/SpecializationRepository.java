package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    @Query("SELECT s FROM Specialization s ORDER BY s.displayOrder ASC NULLS LAST, s.createdAt ASC")
    List<Specialization> findAllOrdered();
}
