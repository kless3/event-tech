package com.ems.importerservice.service;

import com.ems.importerservice.domain.ImportedEvent;
import com.ems.importerservice.dto.ImportedEventResponse;
import org.springframework.stereotype.Component;

@Component
public class ImportedEventMapper {
    public ImportedEventResponse toResponse(ImportedEvent importedEvent) {
        return new ImportedEventResponse(
            importedEvent.getId(),
            importedEvent.getSource(),
            importedEvent.getExternalId(),
            importedEvent.getEventId(),
            importedEvent.getTitle(),
            importedEvent.getStatus(),
            importedEvent.getFailureReason(),
            importedEvent.getImportedAt(),
            importedEvent.getCreatedAt(),
            importedEvent.getUpdatedAt()
        );
    }
}
