package com.workflowhub.integrations_backend.service.google;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workflowhub.integrations_backend.dto.CalendarEventDto;
import com.workflowhub.integrations_backend.entity.CalendarEvent;
import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.repository.CalendarEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleCalendarSyncService {

    private final CalendarEventRepository calendarEventRepository;
    private final GoogleCalendarApiClient googleCalendarApiClient;


    @Transactional
    public void syncEvents(
            Long userId,
            List<CalendarEventDto> events
    ) {
        for (CalendarEventDto dto : events) {
            upsertEvent(userId, dto);
        }
    }

    private void upsertEvent(Long userId, CalendarEventDto dto) {

        calendarEventRepository
                .findByUserIdAndProviderAndProviderEventId(
                        userId,
                        "calendar",
                        dto.getId()
                )
                .ifPresentOrElse(
                        existing -> updateExisting(existing, dto),
                        () -> insertNew(userId, dto)
                );
    }

    private void insertNew(Long userId, CalendarEventDto dto) {

        CalendarEvent event = CalendarEvent.builder()
                .userId(userId)
                .provider("calendar")
                .providerEventId(dto.getId())
                .title(dto.getTitle())
                .startTime(parseTime(dto.getStart()))
                .endTime(parseTime(dto.getEnd()))
                .allDay(isAllDay(dto))
                .metadata(null)
                .status("ACTIVE")
                .lastSyncedAt(Instant.now())
                .build();

        calendarEventRepository.save(event);
    }

    private void updateExisting(CalendarEvent event, CalendarEventDto dto) {

        event.setTitle(dto.getTitle());
        event.setStartTime(parseTime(dto.getStart()));
        event.setEndTime(parseTime(dto.getEnd()));
        event.setAllDay(isAllDay(dto));
        event.setLastSyncedAt(Instant.now());

        calendarEventRepository.save(event);
    }

    private Instant parseTime(String value) {
        return OffsetDateTime.parse(value).toInstant();
    }

    private boolean isAllDay(CalendarEventDto dto) {
        return !dto.getStart().contains("T");
    }
   @Transactional
public void syncForIntegration(Integration integration) {

    Long userId = integration.getUserId();

    // 1️⃣ Fetch events from Google
    List<CalendarEventDto> events =
            googleCalendarApiClient.fetchCalendarEvents(integration);

    // 2️⃣ Sync to DB (reuse existing logic)
    syncEvents(userId, events);
}

}
