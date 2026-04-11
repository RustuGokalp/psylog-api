package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.SpecializationRequest;
import com.gokalp.psylog_api.dto.response.SpecializationResponse;
import com.gokalp.psylog_api.entity.Specialization;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.repository.SpecializationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// TC-01 through TC-09: Unit tests for SpecializationService — verifies business logic in isolation
@ExtendWith(MockitoExtension.class)
class SpecializationServiceTest {

    @Mock
    private SpecializationRepository specializationRepository;

    private SpecializationService specializationService;

    @BeforeEach
    void setup() {
        specializationService = new SpecializationService(specializationRepository);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Specialization buildEntity(Long id, String title, String slug, String summary, String content, String image, Integer displayOrder) {
        Specialization s = new Specialization();
        ReflectionTestUtils.setField(s, "id", id);
        s.setTitle(title);
        s.setSlug(slug);
        s.setSummary(summary);
        s.setContent(content);
        s.setImage(image);
        s.setDisplayOrder(displayOrder);
        ReflectionTestUtils.setField(s, "createdAt", LocalDateTime.of(2026, 3, 22, 10, 0));
        ReflectionTestUtils.setField(s, "updatedAt", LocalDateTime.of(2026, 3, 22, 10, 0));
        return s;
    }

    private SpecializationRequest buildRequest(String title, String summary, String content, String image, Integer displayOrder) {
        SpecializationRequest req = new SpecializationRequest();
        req.setTitle(title);
        req.setSummary(summary);
        req.setContent(content);
        req.setImage(image);
        req.setDisplayOrder(displayOrder);
        return req;
    }

    // ─── getAll ─────────────────────────────────────────────────────────────

    // TC-01: repo returns entities → service maps all fields to response list
    @Test
    void getAll_returnsAllSpecializationsMappedToResponse() {
        Specialization s1 = buildEntity(1L, "Anksiyete", "anksiyete", "Özet 1", "İçerik 1", "https://img.com/1.jpg", 1);
        Specialization s2 = buildEntity(2L, "Depresyon", "depresyon", "Özet 2", "İçerik 2", null, 2);
        when(specializationRepository.findAllOrdered()).thenReturn(List.of(s1, s2));

        List<SpecializationResponse> result = specializationService.getAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Anksiyete", result.get(0).title());
        assertEquals("anksiyete", result.get(0).slug());
        assertEquals("https://img.com/1.jpg", result.get(0).image());
        assertEquals(1, result.get(0).displayOrder());
        assertNull(result.get(1).image());
    }

    // TC-02: repo returns empty list → service returns empty list
    @Test
    void getAll_whenNoneExist_returnsEmptyList() {
        when(specializationRepository.findAllOrdered()).thenReturn(List.of());

        List<SpecializationResponse> result = specializationService.getAll();

        assertTrue(result.isEmpty());
    }

    // ─── getBySlug ──────────────────────────────────────────────────────────

    // TC-03: existing slug → returns mapped response
    @Test
    void getBySlug_existingSlug_returnsResponse() {
        Specialization s = buildEntity(1L, "Anksiyete", "anksiyete", "Özet", "İçerik", null, 1);
        when(specializationRepository.findBySlug("anksiyete")).thenReturn(Optional.of(s));

        SpecializationResponse result = specializationService.getBySlug("anksiyete");

        assertEquals(1L, result.id());
        assertEquals("anksiyete", result.slug());
        assertEquals("Özet", result.summary());
        assertEquals("İçerik", result.content());
    }

    // TC-04: non-existent slug → ResourceNotFoundException thrown
    @Test
    void getBySlug_nonExistentSlug_throwsResourceNotFoundException() {
        when(specializationRepository.findBySlug("yok")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> specializationService.getBySlug("yok"));
    }

    // ─── create ─────────────────────────────────────────────────────────────

    // TC-05: valid request → entity saved with all fields, response returned
    @Test
    void create_savesEntityAndReturnsResponse() {
        SpecializationRequest request = buildRequest("Anksiyete", "Özet", "İçerik", "https://img.com/1.jpg", 1);
        Specialization saved = buildEntity(1L, "Anksiyete", "anksiyete", "Özet", "İçerik", "https://img.com/1.jpg", 1);
        when(specializationRepository.existsBySlug(anyString())).thenReturn(false);
        when(specializationRepository.save(any())).thenReturn(saved);

        SpecializationResponse result = specializationService.create(request);

        ArgumentCaptor<Specialization> captor = ArgumentCaptor.forClass(Specialization.class);
        verify(specializationRepository).save(captor.capture());
        assertEquals("Anksiyete", captor.getValue().getTitle());
        assertEquals("Özet", captor.getValue().getSummary());
        assertEquals("İçerik", captor.getValue().getContent());
        assertEquals("https://img.com/1.jpg", captor.getValue().getImage());
        assertEquals(1, captor.getValue().getDisplayOrder());
        assertEquals(1L, result.id());
        assertEquals("Anksiyete", result.title());
    }

    // TC-06: image is blank → normalizeUrl stores null
    @Test
    void create_withBlankImage_storesNullImage() {
        SpecializationRequest request = buildRequest("Title", "Özet", "İçerik", "   ", null);
        Specialization saved = buildEntity(1L, "Title", "title", "Özet", "İçerik", null, null);
        when(specializationRepository.existsBySlug(anyString())).thenReturn(false);
        when(specializationRepository.save(any())).thenReturn(saved);

        specializationService.create(request);

        ArgumentCaptor<Specialization> captor = ArgumentCaptor.forClass(Specialization.class);
        verify(specializationRepository).save(captor.capture());
        assertNull(captor.getValue().getImage());
    }

    // TC-07: displayOrder is null → saved with null displayOrder
    @Test
    void create_withNullDisplayOrder_savesWithNullDisplayOrder() {
        SpecializationRequest request = buildRequest("Title", "Özet", "İçerik", null, null);
        Specialization saved = buildEntity(1L, "Title", "title", "Özet", "İçerik", null, null);
        when(specializationRepository.existsBySlug(anyString())).thenReturn(false);
        when(specializationRepository.save(any())).thenReturn(saved);

        SpecializationResponse result = specializationService.create(request);

        assertNull(result.displayOrder());
    }

    // ─── update ─────────────────────────────────────────────────────────────

    // TC-08: existing id + valid request → entity fields updated, response returned
    @Test
    void update_existingId_updatesAndReturnsResponse() {
        Specialization existing = buildEntity(1L, "Old Title", "old-title", "Old Özet", "Old İçerik", null, null);
        SpecializationRequest request = buildRequest("New Title", "New Özet", "New İçerik", "https://img.com/new.jpg", 3);
        Specialization saved = buildEntity(1L, "New Title", "new-title", "New Özet", "New İçerik", "https://img.com/new.jpg", 3);

        when(specializationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(specializationRepository.existsBySlugAndIdNot(anyString(), eq(1L))).thenReturn(false);
        when(specializationRepository.save(any())).thenReturn(saved);

        SpecializationResponse result = specializationService.update(1L, request);

        assertEquals("New Title", existing.getTitle());
        assertEquals("New Özet", existing.getSummary());
        assertEquals("New İçerik", existing.getContent());
        assertEquals("https://img.com/new.jpg", existing.getImage());
        assertEquals(3, existing.getDisplayOrder());
        assertEquals(1L, result.id());
    }

    // TC-09: non-existent id → ResourceNotFoundException thrown
    @Test
    void update_nonExistentId_throwsResourceNotFoundException() {
        when(specializationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> specializationService.update(99L, buildRequest("T", "S", "C", null, null)));
        verify(specializationRepository, never()).save(any());
    }

    // ─── delete ─────────────────────────────────────────────────────────────

    // TC-10: existing id → entity deleted
    @Test
    void delete_existingId_deletesEntity() {
        Specialization existing = buildEntity(1L, "Title", "title", "Özet", "İçerik", null, null);
        when(specializationRepository.findById(1L)).thenReturn(Optional.of(existing));

        specializationService.delete(1L);

        verify(specializationRepository).delete(existing);
    }

    // TC-11: non-existent id → ResourceNotFoundException thrown, delete not called
    @Test
    void delete_nonExistentId_throwsResourceNotFoundException() {
        when(specializationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> specializationService.delete(99L));
        verify(specializationRepository, never()).delete(any());
    }
}
