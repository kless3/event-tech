package com.ems.importerservice.config;

import com.ems.importerservice.domain.EventSource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.import")
@Getter
@Setter
public class ImportProperties {
    private Schedule schedule = new Schedule();
    private Map<String, Source> sources = new HashMap<>();

    public Source source(EventSource source) {
        return sources.getOrDefault(source.name().toLowerCase(Locale.ROOT), new Source());
    }

    @Getter
    @Setter
    public static class Schedule {
        private boolean enabled;
        private String organizerUserId;
        private int limit = 20;
        private Duration fixedDelay = Duration.ofHours(6);

        public Optional<UUID> organizerUserId() {
            if (organizerUserId == null || organizerUserId.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(UUID.fromString(organizerUserId));
        }
    }

    @Getter
    @Setter
    public static class Source {
        private boolean enabled = true;
        private String baseUrl;
        private String apiKey;
        private int retryAttempts = 3;
        private Duration retryBackoff = Duration.ofMillis(250);
    }
}
