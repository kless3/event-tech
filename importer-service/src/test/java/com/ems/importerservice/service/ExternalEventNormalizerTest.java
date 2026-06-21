package com.ems.importerservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ems.importerservice.domain.EventSource;
import com.ems.importerservice.dto.ExternalEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ExternalEventNormalizerTest {
    private final ExternalEventNormalizer normalizer = new ExternalEventNormalizer();

    @Test
    void trimsTextAndProtectsEventServiceConstraints() {
        ExternalEvent normalized = normalizer.normalize(
            new ExternalEvent(
                EventSource.TIMEPAD,
                " external-1 ",
                " Imported Event ",
                " ",
                " Warsaw ",
                LocalDateTime.now().minusDays(1),
                0
            )
        );

        assertEquals("external-1", normalized.externalId());
        assertEquals("Imported Event", normalized.title());
        assertEquals("Imported event", normalized.description());
        assertEquals("Warsaw", normalized.location());
        assertEquals(1, normalized.capacity());
        assertTrue(normalized.startsAt().isAfter(LocalDateTime.now()));
    }
}
