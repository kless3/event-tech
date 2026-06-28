package com.ems.importerservice.controller;

import com.ems.importerservice.api.ImportApi;
import com.ems.importerservice.domain.EventSource;
import com.ems.importerservice.dto.ImportRequest;
import com.ems.importerservice.dto.ImportRunResponse;
import com.ems.importerservice.dto.ImportedEventResponse;
import com.ems.importerservice.service.ImporterService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
public class ImportController implements ImportApi {
    private final ImporterService importerService;

    @PostMapping("/{source}")
    @Override
    public ImportRunResponse importEvents(
        @PathVariable EventSource source,
        @Valid @RequestBody ImportRequest request
    ) {
        return importerService.importEvents(source, request.organizerUserId(), request.normalizedLimit());
    }

    @GetMapping("/{source}")
    @Override
    public List<ImportedEventResponse> getImportedEvents(@PathVariable EventSource source) {
        return importerService.getImportedEvents(source);
    }

    @GetMapping("/records/{id}")
    @Override
    public ImportedEventResponse getImportedEvent(@PathVariable UUID id) {
        return importerService.getImportedEvent(id);
    }
}
