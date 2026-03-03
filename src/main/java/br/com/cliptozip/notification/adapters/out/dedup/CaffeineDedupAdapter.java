package br.com.cliptozip.notification.adapters.out.dedup;

import br.com.cliptozip.notification.domain.ports.DedupPort;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CaffeineDedupAdapter implements DedupPort {

    private final Cache<String, Boolean> cache;
    private final boolean enabled;

    public CaffeineDedupAdapter(
            @Value("${app.dedup.enabled:true}") boolean enabled,
            @Value("${app.dedup.ttl-seconds:900}") long ttlSeconds
    ) {
        this.enabled = enabled;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(Math.max(30, ttlSeconds)))
                .maximumSize(50_000)
                .build();
    }

    @Override
    public boolean isDuplicate(String messageKey) {
        if (!enabled) return false;
        return cache.getIfPresent(messageKey) != null;
    }

    @Override
    public void markProcessed(String messageKey) {
        if (!enabled) return;
        cache.put(messageKey, Boolean.TRUE);
    }
}
