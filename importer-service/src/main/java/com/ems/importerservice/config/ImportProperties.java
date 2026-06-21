package com.ems.importerservice.config;

import com.ems.importerservice.domain.EventSource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.import")
public class ImportProperties {
    private Schedule schedule = new Schedule();
    private Map<String, Source> sources = new HashMap<>();

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Map<String, Source> getSources() {
        return sources;
    }

    public void setSources(Map<String, Source> sources) {
        this.sources = sources;
    }

    public Source source(EventSource source) {
        return sources.getOrDefault(source.name().toLowerCase(Locale.ROOT), new Source());
    }

    public static class Schedule {
        private boolean enabled;
        private String organizerUserId;
        private int limit = 20;
        private Duration fixedDelay = Duration.ofHours(6);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Optional<UUID> organizerUserId() {
            if (organizerUserId == null || organizerUserId.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(UUID.fromString(organizerUserId));
        }

        public String getOrganizerUserId() {
            return organizerUserId;
        }

        public void setOrganizerUserId(String organizerUserId) {
            this.organizerUserId = organizerUserId;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public Duration getFixedDelay() {
            return fixedDelay;
        }

        public void setFixedDelay(Duration fixedDelay) {
            this.fixedDelay = fixedDelay;
        }
    }

    public static class Source {
        private boolean enabled = true;
        private String baseUrl;
        private String apiKey;
        private int retryAttempts = 3;
        private Duration retryBackoff = Duration.ofMillis(250);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public int getRetryAttempts() {
            return retryAttempts;
        }

        public void setRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
        }

        public Duration getRetryBackoff() {
            return retryBackoff;
        }

        public void setRetryBackoff(Duration retryBackoff) {
            this.retryBackoff = retryBackoff;
        }
    }
}
