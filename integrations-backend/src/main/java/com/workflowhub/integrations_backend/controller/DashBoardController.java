package com.workflowhub.integrations_backend.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.workflowhub.integrations_backend.dto.DashboardItemDto;
import com.workflowhub.integrations_backend.entity.CalendarEvent;
import com.workflowhub.integrations_backend.repository.CalendarEventRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashBoardController {
    private final CalendarEventRepository calendarEventRepository;
    
    public DashBoardController(CalendarEventRepository calendarEventRepository){
        this.calendarEventRepository = calendarEventRepository;
    }
        //fetch user id
    private Long resolveUserId(HttpHeaders headers) {
        List<String> vals = headers.get("X-User-Id");
        if (vals != null && !vals.isEmpty()) {
            try {
                return Long.valueOf(vals.get(0));
            } catch (NumberFormatException ignore) {}
        }
        return 1L; // dev user
    }

     @GetMapping("/events/today")
    public ResponseEntity<?> getTodayEvents(@RequestHeader HttpHeaders headers) {

        Long userId = resolveUserId(headers);

        // Calculate start & end of today (server timezone)
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<CalendarEvent> events =
                calendarEventRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(
                        userId,
                        startOfDay,
                        endOfDay
                );

        List<DashboardItemDto> dashboardItems = events.stream()
        .map(e -> DashboardItemDto.fromCalendarEvent(e))
        .toList();


        return ResponseEntity.ok(
    Map.of(
        "date", today.toString(),
        "count", dashboardItems.size(),
        "items", dashboardItems
    )
);

    }
}
