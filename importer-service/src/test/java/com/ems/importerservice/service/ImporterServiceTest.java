package com.ems.importerservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ems.importerservice.client.EventServiceClient;
import com.ems.importerservice.client.ExternalEventClient;
import com.ems.importerservice.client.ExternalEventClientRegistry;
import com.ems.importerservice.config.ImportProperties;
import com.ems.importerservice.domain.EventSource;
import com.ems.importerservice.domain.ImportStatus;
import com.ems.importerservice.domain.ImportedEvent;
import com.ems.importerservice.dto.CreateEventRequest;
import com.ems.importerservice.dto.EventResponse;
import com.ems.importerservice.dto.ExternalEvent;
import com.ems.importerservice.dto.ImportRunResponse;
import com.ems.importerservice.exception.EventServiceUnavailableException;
import com.ems.importerservice.repository.ImportedEventRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ImporterServiceTest {
    private final ImportedEventRepository importedEventRepository = Mockito.mock(ImportedEventRepository.class);
    private final EventServiceClient eventServiceClient = Mockito.mock(EventServiceClient.class);
    private final ExternalEventClient externalEventClient = new TestExternalEventClient();
    private final ImportedEventMapper importedEventMapper = new ImportedEventMapper();
    private final ImporterService importerService = new ImporterService(
        importedEventRepository,
        new ExternalEventClientRegistry(List.of(externalEventClient)),
        eventServiceClient,
        new ExternalEventNormalizer(),
        importProperties(),
        importedEventMapper
    );

    @Test
    void importsExternalEventsIntoEventService() {
        UUID organizerUserId = UUID.randomUUID();
        when(importedEventRepository.existsBySourceAndExternalId(EventSource.TICKETMASTER, "external-1")).thenReturn(false);
        when(eventServiceClient.createEvent(any(CreateEventRequest.class))).thenReturn(eventResponse(organizerUserId));
        when(importedEventRepository.save(any(ImportedEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportRunResponse response = importerService.importEvents(EventSource.TICKETMASTER, organizerUserId, 1);

        assertEquals(1, response.imported());
        assertEquals(0, response.skipped());
        assertEquals(0, response.failed());
        assertEquals(ImportStatus.IMPORTED, response.records().getFirst().status());
        verify(eventServiceClient).createEvent(any(CreateEventRequest.class));
    }

    @Test
    void skipsAlreadyImportedExternalEvents() {
        UUID organizerUserId = UUID.randomUUID();
        when(importedEventRepository.existsBySourceAndExternalId(EventSource.TICKETMASTER, "external-1")).thenReturn(true);

        ImportRunResponse response = importerService.importEvents(EventSource.TICKETMASTER, organizerUserId, 1);

        assertEquals(0, response.imported());
        assertEquals(1, response.skipped());
        assertEquals(0, response.failed());
        verify(eventServiceClient, never()).createEvent(any(CreateEventRequest.class));
    }

    @Test
    void storesFailedImportWhenEventServiceFails() {
        UUID organizerUserId = UUID.randomUUID();
        when(importedEventRepository.existsBySourceAndExternalId(EventSource.TICKETMASTER, "external-1")).thenReturn(false);
        when(eventServiceClient.createEvent(any(CreateEventRequest.class)))
            .thenThrow(new EventServiceUnavailableException("Event Service request failed"));
        when(importedEventRepository.save(any(ImportedEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportRunResponse response = importerService.importEvents(EventSource.TICKETMASTER, organizerUserId, 1);

        assertEquals(0, response.imported());
        assertEquals(0, response.skipped());
        assertEquals(1, response.failed());
        assertEquals(ImportStatus.FAILED, response.records().getFirst().status());
    }

    @Test
    void retriesExternalSourceFetchBeforeImporting() {
        ImportedEventRepository repository = Mockito.mock(ImportedEventRepository.class);
        EventServiceClient eventClient = Mockito.mock(EventServiceClient.class);
        FlakyExternalEventClient flakyClient = new FlakyExternalEventClient();
        ImporterService service = new ImporterService(
            repository,
            new ExternalEventClientRegistry(List.of(flakyClient)),
            eventClient,
            new ExternalEventNormalizer(),
            importProperties(),
            importedEventMapper
        );
        UUID organizerUserId = UUID.randomUUID();
        when(repository.existsBySourceAndExternalId(EventSource.TICKETMASTER, "external-1")).thenReturn(false);
        when(eventClient.createEvent(any(CreateEventRequest.class))).thenReturn(eventResponse(organizerUserId));
        when(repository.save(any(ImportedEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportRunResponse response = service.importEvents(EventSource.TICKETMASTER, organizerUserId, 1);

        assertEquals(2, flakyClient.fetchAttempts);
        assertEquals(1, response.imported());
    }

    private EventResponse eventResponse(UUID organizerUserId) {
        return new EventResponse(
            UUID.randomUUID(),
            organizerUserId,
            "Imported Event",
            "Imported",
            "Warsaw",
            LocalDateTime.now().plusDays(30),
            100,
            0,
            "PUBLISHED"
        );
    }

    private ImportProperties importProperties() {
        ImportProperties properties = new ImportProperties();
        ImportProperties.Source source = new ImportProperties.Source();
        source.setRetryAttempts(3);
        source.setRetryBackoff(Duration.ZERO);
        properties.getSources().put("ticketmaster", source);
        return properties;
    }

    private static class TestExternalEventClient implements ExternalEventClient {
        @Override
        public EventSource source() {
            return EventSource.TICKETMASTER;
        }

        @Override
        public List<ExternalEvent> fetchEvents(int limit) {
            return List.of(
                new ExternalEvent(
                    EventSource.TICKETMASTER,
                    "external-1",
                    "Imported Event",
                    "Imported",
                    "Warsaw",
                    LocalDateTime.now().plusDays(30),
                    100
                )
            ).stream().limit(limit).toList();
        }
    }

    private static class FlakyExternalEventClient extends TestExternalEventClient {
        private int fetchAttempts = 0;

        @Override
        public List<ExternalEvent> fetchEvents(int limit) {
            fetchAttempts++;
            if (fetchAttempts == 1) {
                throw new IllegalStateException("Ticketmaster throttled request");
            }
            return super.fetchEvents(limit);
        }
    }
}
