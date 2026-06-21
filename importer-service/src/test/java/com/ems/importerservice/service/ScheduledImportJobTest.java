package com.ems.importerservice.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ems.importerservice.config.ImportProperties;
import com.ems.importerservice.domain.EventSource;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ScheduledImportJobTest {
    @Test
    void importsEnabledSourcesWhenScheduleIsConfigured() {
        ImportProperties properties = new ImportProperties();
        properties.getSchedule().setEnabled(true);
        properties.getSchedule().setOrganizerUserId(UUID.randomUUID().toString());
        properties.getSchedule().setLimit(10);
        ImportProperties.Source ticketmaster = new ImportProperties.Source();
        ticketmaster.setEnabled(true);
        ImportProperties.Source timepad = new ImportProperties.Source();
        timepad.setEnabled(false);
        properties.getSources().put("ticketmaster", ticketmaster);
        properties.getSources().put("timepad", timepad);
        ImporterService importerService = Mockito.mock(ImporterService.class);

        new ScheduledImportJob(properties, importerService).importEnabledSources();

        verify(importerService).importEvents(EventSource.TICKETMASTER, properties.getSchedule().organizerUserId().orElseThrow(), 10);
        verify(importerService, never()).importEvents(EventSource.TIMEPAD, properties.getSchedule().organizerUserId().orElseThrow(), 10);
    }
}
