package com.workflowhub.integrations_backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workflowhub.integrations_backend.entity.CalendarEvent;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
     // Find a specific provider event (used to avoid duplicates on sync)
    Optional<CalendarEvent> findByUserIdAndProviderAndProviderEventId(
            Long userId,
            String provider,
            String providerEventId
    );

    // Fetch events for dashboard (today / upcoming)
    List<CalendarEvent> findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(
            Long userId,
            Instant start,
            Instant end
    );

    // Fetch all events from a provider (history / re-sync)
    List<CalendarEvent> findByUserIdAndProvider(
            Long userId,
            String provider
    );

       
    
    List<CalendarEvent> findByUserIdAndProviderOrderByStartTimeDesc(
            Long userId,
            String provider
    );
}
