package br.com.cliptozip.notification.adapters.in.sqs;

import br.com.cliptozip.notification.domain.NotificationEvent;
import br.com.cliptozip.notification.domain.service.NotifyUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * SQS inbound adapter.
 *
 * We accept the raw message payload as String because:
 *  - in LocalStack + PowerShell, it's common to send a payload that isn't strict JSON (missing quotes)
 *  - we want full control over parsing and better error logs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationSqsListener {

    private final ObjectMapper objectMapper;
    private final NotifyUserService notifyUserService;

    // {titulo:Teste,status:OK} -> {"titulo":"Teste","status":"OK"}
    private static final Pattern UNQUOTED_KEYS = Pattern.compile("([\\{,])\\s*([A-Za-z0-9_]+)\\s*:");
    private static final Pattern UNQUOTED_SIMPLE_VALUES = Pattern.compile(":\\s*([^\"\\{\\[\\]\\},\\s][^,}]*)\\s*([,}])");

    // keep aligned with application.yml: app.sqs.queue-name
    @SqsListener("${app.sqs.queue-name}")
    public void onMessage(Message<String> message) {
        String payload = message.getPayload();
        String messageId = extractMessageId(message.getHeaders());

        log.info("Received SQS messageId={} payload={}", messageId, payload);

        NotificationEvent event = parsePayload(payload);
        notifyUserService.notify(event, messageId);
    }

    private NotificationEvent parsePayload(String rawPayload) {
        String payload = rawPayload == null ? "" : rawPayload.trim();

        // 1) strict JSON first
        try {
            return objectMapper.readValue(payload, NotificationEvent.class);
        } catch (JsonProcessingException ignored) {
            // try repair below
        }

        // 2) try to repair common pseudo-json shapes produced by PowerShell/CLI usage
        String repaired = repairToJson(payload);
        try {
            return objectMapper.readValue(repaired, NotificationEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse SQS payload. raw='{}' repaired='{}'", payload, repaired);
            throw new IllegalArgumentException("Invalid SQS payload. Expected JSON for NotificationEvent.", e);
        }
    }

    private static String repairToJson(String payload) {
        if (payload == null) return "";
        String s = payload.trim();
        if (!s.startsWith("{") || !s.endsWith("}")) {
            return s;
        }

        // quote keys
        s = UNQUOTED_KEYS.matcher(s).replaceAll("$1\"$2\":");

        // quote simple (non-json) values that are not already quoted/objects/arrays
        s = UNQUOTED_SIMPLE_VALUES.matcher(s).replaceAll(match -> {
            String value = match.group(1).trim();
            String end = match.group(2);

            // keep literals
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value)) {
                return ":" + value + end;
            }

            // keep numbers
            if (value.matches("-?\\d+(\\.\\d+)?")) {
                return ":" + value + end;
            }

            // escape quotes/backslashes
            value = value.replace("\\\\", "\\\\\\\\").replace("\"", "\\\\\"");
            return ":\"" + value + "\"" + end;
        });

        return s;
    }

    private static String extractMessageId(Map<String, Object> headers) {
        if (headers == null || headers.isEmpty()) return null;

        // common candidates
        Object v = headers.get("MessageId");
        if (v == null) v = headers.get("messageId");
        if (v == null) v = headers.get("SqsMessageId");
        if (v == null) v = headers.get("sqs_message_id");
        if (v == null) {
            // fallback: find any header key that contains "message" and "id"
            for (Map.Entry<String, Object> e : headers.entrySet()) {
                String k = e.getKey();
                if (k == null) continue;
                String lower = k.toLowerCase();
                if (lower.contains("message") && lower.contains("id")) {
                    v = e.getValue();
                    break;
                }
            }
        }
        return v == null ? null : String.valueOf(v);
    }
}
