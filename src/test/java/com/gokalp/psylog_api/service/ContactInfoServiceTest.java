package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.ContactInfoRequest;
import com.gokalp.psylog_api.dto.request.WorkingHourRequest;
import com.gokalp.psylog_api.dto.response.ContactInfoResponse;
import com.gokalp.psylog_api.entity.ContactInfo;
import com.gokalp.psylog_api.entity.WorkingHour;
import com.gokalp.psylog_api.entity.WorkingHourStatus;
import com.gokalp.psylog_api.repository.ContactInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests for ContactInfoService — focuses on the working-hours handling
// added on top of the existing single-row contact info.
@ExtendWith(MockitoExtension.class)
class ContactInfoServiceTest {

    @Mock
    private ContactInfoRepository contactInfoRepository;

    private ContactInfoService service() {
        return new ContactInfoService(contactInfoRepository);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private WorkingHourRequest hour(DayOfWeek day, WorkingHourStatus status, LocalTime start, LocalTime end) {
        WorkingHourRequest req = new WorkingHourRequest();
        req.setDayOfWeek(day);
        req.setStatus(status);
        req.setStartTime(start);
        req.setEndTime(end);
        return req;
    }

    // Mon–Fri OPEN 09:00–18:00, Sat BY_APPOINTMENT, Sun CLOSED.
    private List<WorkingHourRequest> fullWeek() {
        List<WorkingHourRequest> list = new ArrayList<>();
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            list.add(hour(d, WorkingHourStatus.OPEN, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        }
        list.add(hour(DayOfWeek.SATURDAY, WorkingHourStatus.BY_APPOINTMENT, null, null));
        list.add(hour(DayOfWeek.SUNDAY, WorkingHourStatus.CLOSED, null, null));
        return list;
    }

    private ContactInfoRequest request(List<WorkingHourRequest> hours) {
        ContactInfoRequest req = new ContactInfoRequest();
        req.setPhone("532-111-22-33");
        req.setEmail("info@example.com");
        req.setLocation("İstanbul");
        req.setInstagram("drpsikolog");
        req.setWorkingHours(hours);
        return req;
    }

    // ─── create ───────────────────────────────────────────────────────────────

    // A valid full week is persisted as 7 WorkingHour rows wired to the parent,
    // with hours kept only for OPEN days.
    @Test
    void createContactInfo_persistsAllSevenWorkingHours() {
        when(contactInfoRepository.findFirstBy()).thenReturn(Optional.empty());
        when(contactInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service().createContactInfo(request(fullWeek()));

        ArgumentCaptor<ContactInfo> captor = ArgumentCaptor.forClass(ContactInfo.class);
        verify(contactInfoRepository).save(captor.capture());
        List<WorkingHour> saved = captor.getValue().getWorkingHours();

        assertEquals(7, saved.size());
        saved.forEach(wh -> assertSame(captor.getValue(), wh.getContactInfo(), "parent must be wired"));

        WorkingHour monday = saved.stream().filter(w -> w.getDayOfWeek() == DayOfWeek.MONDAY).findFirst().orElseThrow();
        assertEquals(WorkingHourStatus.OPEN, monday.getStatus());
        assertEquals(LocalTime.of(9, 0), monday.getStartTime());
        assertEquals(LocalTime.of(18, 0), monday.getEndTime());

        WorkingHour saturday = saved.stream().filter(w -> w.getDayOfWeek() == DayOfWeek.SATURDAY).findFirst().orElseThrow();
        assertEquals(WorkingHourStatus.BY_APPOINTMENT, saturday.getStatus());
        assertNull(saturday.getStartTime());
        assertNull(saturday.getEndTime());

        WorkingHour sunday = saved.stream().filter(w -> w.getDayOfWeek() == DayOfWeek.SUNDAY).findFirst().orElseThrow();
        assertEquals(WorkingHourStatus.CLOSED, sunday.getStatus());
        assertNull(sunday.getStartTime());
    }

    // Hours sent for a non-OPEN day are ignored (stored as null), guarding the API
    // even if a client bypasses request-level validation.
    @Test
    void createContactInfo_nonOpenDayWithHours_storesNull() {
        List<WorkingHourRequest> week = fullWeek();
        week.set(6, hour(DayOfWeek.SUNDAY, WorkingHourStatus.CLOSED, LocalTime.of(10, 0), LocalTime.of(12, 0)));
        when(contactInfoRepository.findFirstBy()).thenReturn(Optional.empty());
        when(contactInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service().createContactInfo(request(week));

        ArgumentCaptor<ContactInfo> captor = ArgumentCaptor.forClass(ContactInfo.class);
        verify(contactInfoRepository).save(captor.capture());
        WorkingHour sunday = captor.getValue().getWorkingHours().stream()
                .filter(w -> w.getDayOfWeek() == DayOfWeek.SUNDAY).findFirst().orElseThrow();
        assertNull(sunday.getStartTime());
        assertNull(sunday.getEndTime());
    }

    // Missing a day → rejected, nothing saved.
    @Test
    void createContactInfo_incompleteWeek_throwsAndDoesNotSave() {
        when(contactInfoRepository.findFirstBy()).thenReturn(Optional.empty());
        List<WorkingHourRequest> week = fullWeek();
        week.remove(0); // drop Monday → only 6 days

        assertThrows(IllegalArgumentException.class, () -> service().createContactInfo(request(week)));
        verify(contactInfoRepository, never()).save(any());
    }

    // A duplicated day (e.g. two Mondays) → rejected, nothing saved.
    @Test
    void createContactInfo_duplicateDay_throwsAndDoesNotSave() {
        when(contactInfoRepository.findFirstBy()).thenReturn(Optional.empty());
        List<WorkingHourRequest> week = fullWeek();
        week.set(6, hour(DayOfWeek.MONDAY, WorkingHourStatus.CLOSED, null, null)); // Sunday → second Monday

        assertThrows(IllegalArgumentException.class, () -> service().createContactInfo(request(week)));
        verify(contactInfoRepository, never()).save(any());
    }

    // ─── update ───────────────────────────────────────────────────────────────

    // Update replaces existing working hours (old rows cleared) and returns the
    // 7 days ordered by dayOfWeek in the response.
    @Test
    void updateContactInfo_replacesHoursAndReturnsOrderedResponse() {
        ContactInfo existing = new ContactInfo();
        WorkingHour stale = new WorkingHour();
        stale.setContactInfo(existing);
        stale.setDayOfWeek(DayOfWeek.MONDAY);
        stale.setStatus(WorkingHourStatus.CLOSED);
        existing.getWorkingHours().add(stale);

        when(contactInfoRepository.findFirstBy()).thenReturn(Optional.of(existing));
        when(contactInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContactInfoResponse response = service().updateContactInfo(request(fullWeek()));

        assertEquals(7, existing.getWorkingHours().size());
        assertFalse(existing.getWorkingHours().contains(stale), "stale row should be cleared");
        assertEquals(7, response.getWorkingHours().size());
        assertEquals(DayOfWeek.MONDAY, response.getWorkingHours().get(0).getDayOfWeek());
        assertEquals("drpsikolog", response.getInstagram());
    }

    // ─── instagram ──────────────────────────────────────────────────────────────

    // Instagram is optional: a null value is accepted and persisted as null
    // without errors, keeping the existing single row intact.
    @Test
    void createContactInfo_nullInstagram_savesWithNull() {
        ContactInfoRequest req = request(fullWeek());
        req.setInstagram(null);
        when(contactInfoRepository.findFirstBy()).thenReturn(Optional.empty());
        when(contactInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContactInfoResponse response = service().createContactInfo(req);

        ArgumentCaptor<ContactInfo> captor = ArgumentCaptor.forClass(ContactInfo.class);
        verify(contactInfoRepository).save(captor.capture());
        assertNull(captor.getValue().getInstagram());
        assertNull(response.getInstagram());
    }
}
