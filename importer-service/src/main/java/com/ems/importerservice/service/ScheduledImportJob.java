package com.ems.importerservice.service;

import com.ems.importerservice.config.ImportProperties;
import com.ems.importerservice.domain.EventSource;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledImportJob {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ImportProperties importProperties;
    private final ImporterService importerService;

    public ScheduledImportJob(ImportProperties importProperties, ImporterService importerService) {
        this.importProperties = importProperties;
        this.importerService = importerService;
    }

    @Scheduled(fixedDelayString = "${app.import.schedule.fixed-delay:PT6H}")
    public void importEnabledSources() {
        ImportProperties.Schedule schedule = importProperties.getSchedule();
        if (!schedule.isEnabled()) {
            return;
        }

        UUID organizerUserId = schedule.organizerUserId().orElse(null);
        if (organizerUserId == null) {
            log.warn("Scheduled import is enabled but organizer user id is not configured");
            return;
        }

        for (EventSource source : EventSource.values()) {
            ImportProperties.Source sourceProperties = importProperties.source(source);
            if (!sourceProperties.isEnabled()) {
                continue;
            }
            importerService.importEvents(source, organizerUserId, schedule.getLimit());
        }
    }
}
