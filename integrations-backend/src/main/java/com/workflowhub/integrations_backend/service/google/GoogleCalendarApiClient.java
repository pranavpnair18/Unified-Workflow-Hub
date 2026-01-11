package com.workflowhub.integrations_backend.service.google;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowhub.integrations_backend.dto.CalendarEventDto;
import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.service.OAuthTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleCalendarApiClient {

//     private static final String EVENTS_URL =
//             "https://www.googleapis.com/calendar/v3/calendars/primary/events"
//   + "?singleEvents=true"
//   + "&orderBy=startTime"
//   + "&maxResults=50"
//   + "&timeMin=" + URLEncoder.encode(
//         OffsetDateTime.now().minusDays(7).toString(),
//         StandardCharsets.UTF_8
//     );

    private final OAuthTokenService oauthTokenService;
    private final ObjectMapper objectMapper;
    private static final String BASE_EVENTS_URL =
    "https://www.googleapis.com/calendar/v3/calendars/primary/events";


    public List<CalendarEventDto> fetchCalendarEvents(Integration integration) {


        OffsetDateTime startOfToday =
        OffsetDateTime.now()
                .toLocalDate()
                .atStartOfDay()
                .atOffset(OffsetDateTime.now().getOffset());

String timeMin = URLEncoder.encode(
        startOfToday.toInstant().toString(),
        StandardCharsets.UTF_8
);

String url =
        BASE_EVENTS_URL
        + "?singleEvents=true"
        + "&orderBy=startTime"
        + "&maxResults=50"
        + "&timeMin=" + timeMin;


        try {
            // 1️⃣ Get valid access token (refresh if needed)
            String accessToken =
                    oauthTokenService.getValidAccessToken(integration);

            // 2️⃣ Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Authorization", "Bearer " + accessToken)
        .GET()
        .build();


            // 3️⃣ Execute request
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            // 4️⃣ Parse JSON
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode items = root.get("items");

            List<CalendarEventDto> events = new ArrayList<>();

             

    if (!events.isEmpty()) {
        System.out.println("📅 First event ID = " + events.get(0).getId());
        System.out.println("📅 First event start = " + events.get(0).getStart());
    }

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    CalendarEventDto dto = new CalendarEventDto(
                            item.get("id").asText(),
                            item.get("summary").asText(""),
                            extractDateTime(item.get("start")),
                            extractDateTime(item.get("end"))
                    );
                    events.add(dto);
                }
            }
            System.out.println("📅 Google Calendar API returned events count = " + events.size());

            return events;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch Google Calendar events", ex);
        }
    }

    private String extractDateTime(JsonNode node) {
        if (node == null) return null;

        if (node.has("dateTime")) {
            return node.get("dateTime").asText();
        }
        if (node.has("date")) {
            return node.get("date").asText();
        }
        return null;
    }
}
