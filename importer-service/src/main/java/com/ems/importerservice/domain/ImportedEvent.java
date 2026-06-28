package com.ems.importerservice.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("imported_events")
@CompoundIndexes({
    @CompoundIndex(name = "uk_imported_events_source_external_id", def = "{'source': 1, 'externalId': 1}", unique = true),
    @CompoundIndex(name = "idx_imported_events_status_created_at", def = "{'status': 1, 'createdAt': -1}")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportedEvent {
    @Id
    private UUID id = UUID.randomUUID();

    @Indexed
    private EventSource source;

    @Indexed
    private String externalId;

    private UUID eventId;

    private String title;

    @Indexed
    private ImportStatus status;

    private String failureReason;

    private LocalDateTime importedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public ImportedEvent(EventSource source, String externalId, String title) {
        this.source = source;
        this.externalId = externalId;
        this.title = title;
        this.status = ImportStatus.FAILED;
    }

    public void markImported(UUID eventId, LocalDateTime importedAt) {
        this.eventId = eventId;
        this.status = ImportStatus.IMPORTED;
        this.failureReason = null;
        this.importedAt = importedAt;
    }

    public void markFailed(String reason) {
        this.status = ImportStatus.FAILED;
        this.failureReason = reason.length() > 1024 ? reason.substring(0, 1024) : reason;
    }

}
