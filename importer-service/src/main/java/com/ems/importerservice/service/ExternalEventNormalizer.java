package com.ems.importerservice.service;

import com.ems.importerservice.dto.ExternalEvent;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ExternalEventNormalizer {
    public ExternalEvent normalize(ExternalEvent event) {
        return new ExternalEvent(
            event.source(),
            event.externalId().trim(),
            event.title().trim(),
            normalizeDescription(event.description()),
            event.location().trim(),
            normalizeStartDate(event.startsAt()),
            Math.max(event.capacity(), 1)
        );
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return "Imported event";
        }
        return description.trim();
    }

    private LocalDateTime normalizeStartDate(LocalDateTime startsAt) {
        if (startsAt.isAfter(LocalDateTime.now())) {
            return startsAt;
        }
        return LocalDateTime.now().plusDays(1);
    }
}
