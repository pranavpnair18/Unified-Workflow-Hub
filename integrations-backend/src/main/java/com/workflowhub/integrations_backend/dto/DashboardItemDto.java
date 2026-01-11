package com.workflowhub.integrations_backend.dto;

import java.time.Instant;

import com.workflowhub.integrations_backend.entity.CalendarEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardItemDto {

    // common identity
    private String id;
    private String type;       // CALENDAR, TASK, EMAIL
    private String provider;   // google, github, gmail

    // display
    private String title;
    private String description;

    // time
    private Instant startTime;
    private Instant endTime;

    // status
    private String status;     // ACTIVE, COMPLETED, CANCELLED

    public static DashboardItemDto fromCalendarEvent(CalendarEvent e) {

    DashboardItemDto dto = new DashboardItemDto();

    dto.setId(String.valueOf(e.getId()));
    dto.setType("CALENDAR");
    dto.setProvider("google");

    dto.setTitle(e.getTitle());
    dto.setDescription(null); // calendar events may not have descriptions

    dto.setStartTime(e.getStartTime());
    dto.setEndTime(e.getEndTime());

    dto.setStatus(e.getStatus());

    return dto;
}


}
