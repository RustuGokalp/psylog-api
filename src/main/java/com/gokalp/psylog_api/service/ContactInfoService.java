package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.ContactInfoRequest;
import com.gokalp.psylog_api.dto.request.WorkingHourRequest;
import com.gokalp.psylog_api.dto.response.ContactInfoResponse;
import com.gokalp.psylog_api.entity.ContactInfo;
import com.gokalp.psylog_api.entity.WorkingHour;
import com.gokalp.psylog_api.entity.WorkingHourStatus;
import com.gokalp.psylog_api.exception.AlreadyExistsException;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.repository.ContactInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;

@Service
public class ContactInfoService {

    private static final Logger log = LoggerFactory.getLogger(ContactInfoService.class);

    private final ContactInfoRepository contactInfoRepository;

    public ContactInfoService(ContactInfoRepository contactInfoRepository) {
        this.contactInfoRepository = contactInfoRepository;
    }

    @Transactional(readOnly = true)
    public ContactInfoResponse getContactInfo() {
        ContactInfo contactInfo = contactInfoRepository.findFirstBy()
                .orElseThrow(() -> new ResourceNotFoundException("Contact info not found"));
        log.info("Contact info fetched");
        return new ContactInfoResponse(contactInfo);
    }

    @Transactional
    public ContactInfoResponse createContactInfo(ContactInfoRequest request) {
        if (contactInfoRepository.findFirstBy().isPresent()) {
            throw new AlreadyExistsException("Contact info already exists");
        }
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setPhone(request.getPhone());
        contactInfo.setEmail(request.getEmail());
        contactInfo.setLocation(request.getLocation());
        applyWorkingHours(contactInfo, request.getWorkingHours());
        ContactInfo saved = contactInfoRepository.save(contactInfo);
        log.info("Contact info created: [id={}]", saved.getId());
        return new ContactInfoResponse(saved);
    }

    @Transactional
    public ContactInfoResponse updateContactInfo(ContactInfoRequest request) {
        ContactInfo contactInfo = contactInfoRepository.findFirstBy()
                .orElseThrow(() -> new ResourceNotFoundException("Contact info not found"));
        contactInfo.setPhone(request.getPhone());
        contactInfo.setEmail(request.getEmail());
        contactInfo.setLocation(request.getLocation());
        applyWorkingHours(contactInfo, request.getWorkingHours());
        ContactInfo saved = contactInfoRepository.save(contactInfo);
        log.info("Contact info updated: [id={}]", saved.getId());
        return new ContactInfoResponse(saved);
    }

    @Transactional
    public void deleteContactInfo() {
        ContactInfo contactInfo = contactInfoRepository.findFirstBy()
                .orElseThrow(() -> new ResourceNotFoundException("Contact info not found"));
        contactInfoRepository.delete(contactInfo);
        log.info("Contact info deleted: [id={}]", contactInfo.getId());
    }

    // Haftanın 7 gününü eksiksiz/tekrarsız doğrular, ardından mevcut çalışma
    // saatlerini değiştirir (orphanRemoval ile eskileri silinir). OPEN dışı
    // günlerde saatler null bırakılır.
    private void applyWorkingHours(ContactInfo contactInfo, List<WorkingHourRequest> requests) {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (WorkingHourRequest req : requests) {
            if (!days.add(req.getDayOfWeek())) {
                throw new IllegalArgumentException("Aynı gün birden fazla kez gönderildi: " + req.getDayOfWeek());
            }
        }
        if (!days.equals(EnumSet.allOf(DayOfWeek.class))) {
            throw new IllegalArgumentException("Haftanın 7 günü de eksiksiz gönderilmeli");
        }

        contactInfo.getWorkingHours().clear();
        for (WorkingHourRequest req : requests) {
            WorkingHour wh = new WorkingHour();
            wh.setContactInfo(contactInfo);
            wh.setDayOfWeek(req.getDayOfWeek());
            wh.setStatus(req.getStatus());
            if (req.getStatus() == WorkingHourStatus.OPEN) {
                wh.setStartTime(req.getStartTime());
                wh.setEndTime(req.getEndTime());
            }
            contactInfo.getWorkingHours().add(wh);
        }
    }
}
