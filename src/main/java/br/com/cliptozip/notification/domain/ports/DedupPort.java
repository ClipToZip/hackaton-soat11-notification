package br.com.cliptozip.notification.domain.ports;

public interface DedupPort {
    boolean isDuplicate(String messageKey);
    void markProcessed(String messageKey);
}
