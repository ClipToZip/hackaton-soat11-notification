package br.com.cliptozip.notification.adapters.out.dedup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaffeineDedupAdapterTest {

    @Test
    void whenEnabledShouldDetectDuplicatesAfterMarking() {
        var dedup = new CaffeineDedupAdapter(true, 60);

        assertFalse(dedup.isDuplicate("k1"));
        dedup.markProcessed("k1");
        assertTrue(dedup.isDuplicate("k1"));
        assertFalse(dedup.isDuplicate("k2"));
    }

    @Test
    void whenDisabledShouldNeverReportDuplicates() {
        var dedup = new CaffeineDedupAdapter(false, 60);

        assertFalse(dedup.isDuplicate("k1"));
        dedup.markProcessed("k1");
        assertFalse(dedup.isDuplicate("k1"));
    }
}
