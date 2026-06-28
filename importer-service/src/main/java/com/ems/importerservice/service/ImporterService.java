package com.ems.importerservice.service;

import com.ems.importerservice.client.EventServiceClient;
import com.ems.importerservice.client.ExternalEventClient;
import com.ems.importerservice.client.ExternalEventClientRegistry;
import com.ems.importerservice.config.ImportProperties;
import com.ems.importerservice.domain.EventSource;
import com.ems.importerservice.domain.ImportedEvent;
import com.ems.importerservice.dto.CreateEventRequest;
import com.ems.importerservice.dto.EventResponse;
import com.ems.importerservice.dto.ExternalEvent;
import com.ems.importerservice.dto.ImportRunResponse;
import com.ems.importerservice.dto.ImportedEventResponse;
import com.ems.importerservice.exception.ImportedEventNotFoundException;
import com.ems.importerservice.repository.ImportedEventRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImporterService {
    private final ImportedEventRepository importedEventRepository;
    private final ExternalEventClientRegistry externalEventClientRegistry;
    private final EventServiceClient eventServiceClient;
    private final ExternalEventNormalizer externalEventNormalizer;
    private final ImportProperties importProperties;
    private final ImportedEventMapper importedEventMapper;

    public ImportRunResponse importEvents(EventSource source, UUID organizerUserId, int limit) {
        ExternalEventClient client = externalEventClientRegistry.get(source);
        List<ImportedEventResponse> records = new ArrayList<>();
        int imported = 0;
        int skipped = 0;
        int failed = 0;

        for (ExternalEvent externalEvent : fetchEventsWithRetry(source, client, limit)) {
            ExternalEvent normalizedEvent = externalEventNormalizer.normalize(externalEvent);
            if (importedEventRepository.existsBySourceAndExternalId(source, normalizedEvent.externalId())) {
                skipped++;
                continue;
            }

            ImportedEvent importedEvent = new ImportedEvent(source, normalizedEvent.externalId(), normalizedEvent.title());
            try {
                EventResponse event = eventServiceClient.createEvent(toCreateEventRequest(normalizedEvent, organizerUserId));
                importedEvent.markImported(event.id(), LocalDateTime.now());
                imported++;
            } catch (RuntimeException exception) {
                importedEvent.markFailed(exception.getMessage() == null ? "Import failed" : exception.getMessage());
                failed++;
            }
            records.add(importedEventMapper.toResponse(importedEventRepository.save(importedEvent)));
        }

        return new ImportRunResponse(source, imported, skipped, failed, records);
    }

    public List<ImportedEventResponse> getImportedEvents(EventSource source) {
        return importedEventRepository.findAllBySourceOrderByCreatedAtDesc(source).stream()
            .map(importedEventMapper::toResponse)
            .toList();
    }

    public ImportedEventResponse getImportedEvent(UUID id) {
        return importedEventRepository.findById(id)
            .map(importedEventMapper::toResponse)
            .orElseThrow(() -> new ImportedEventNotFoundException(id));
    }

    private CreateEventRequest toCreateEventRequest(ExternalEvent externalEvent, UUID organizerUserId) {
        return new CreateEventRequest(
            organizerUserId,
            externalEvent.title().trim(),
            externalEvent.description(),
            externalEvent.location().trim(),
            externalEvent.startsAt(),
            externalEvent.capacity(),
            "Imported from " + externalEvent.source() + " externalId=" + externalEvent.externalId()
        );
    }

    private List<ExternalEvent> fetchEventsWithRetry(EventSource source, ExternalEventClient client, int limit) {
        ImportProperties.Source sourceProperties = importProperties.source(source);
        int maxAttempts = Math.max(1, sourceProperties.getRetryAttempts());
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return client.fetchEvents(limit);
            } catch (RuntimeException exception) {
                lastFailure = exception;
                sleepBeforeRetry(sourceProperties, attempt, maxAttempts);
            }
        }

        throw lastFailure == null ? new IllegalStateException("External import failed") : lastFailure;
    }

    private void sleepBeforeRetry(ImportProperties.Source sourceProperties, int attempt, int maxAttempts) {
        if (attempt >= maxAttempts || sourceProperties.getRetryBackoff().isZero()) {
            return;
        }
        try {
            Thread.sleep(sourceProperties.getRetryBackoff().toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("External import retry interrupted", exception);
        }
    }
}
