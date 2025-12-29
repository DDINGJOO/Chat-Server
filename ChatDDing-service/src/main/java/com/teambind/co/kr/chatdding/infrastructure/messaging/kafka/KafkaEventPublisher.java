package com.teambind.co.kr.chatdding.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher;
import com.teambind.co.kr.chatdding.domain.event.ChatEvent;
import com.teambind.co.kr.chatdding.domain.event.InquiryCreatedEvent;
import com.teambind.co.kr.chatdding.domain.event.MessageReadEvent;
import com.teambind.co.kr.chatdding.domain.event.MessageSentEvent;
import com.teambind.co.kr.chatdding.domain.event.SupportAgentAssignedEvent;
import com.teambind.co.kr.chatdding.domain.event.SupportChatClosedEvent;
import com.teambind.co.kr.chatdding.domain.event.SupportRequestCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka 이벤트 발행 Adapter
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private static final String TOPIC_MESSAGE_SENT = "chat-message-sent";
    private static final String TOPIC_MESSAGE_READ = "chat-message-read";
    private static final String TOPIC_SUPPORT_REQUESTED = "support-requested";
    private static final String TOPIC_SUPPORT_AGENT_ASSIGNED = "support-agent-assigned";
    private static final String TOPIC_SUPPORT_CLOSED = "support-closed";
    private static final String TOPIC_DEFAULT = "chat-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(ChatEvent event) {
        String topic = resolveTopicFor(event);
        publish(topic, event);
    }

    @Override
    public void publish(String topic, ChatEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String key = extractKeyFrom(event);

            kafkaTemplate.send(topic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event to topic {}: {}", topic, event.getEventType(), ex);
                        } else {
                            log.debug("Event published to topic {}: {} with key {}",
                                    topic, event.getEventType(), key);
                        }
                    });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventType(), e);
        }
    }

    private String resolveTopicFor(ChatEvent event) {
        return switch (event) {
            case MessageSentEvent ignored -> TOPIC_MESSAGE_SENT;
            case MessageReadEvent ignored -> TOPIC_MESSAGE_READ;
            case SupportRequestCreatedEvent ignored -> TOPIC_SUPPORT_REQUESTED;
            case SupportAgentAssignedEvent ignored -> TOPIC_SUPPORT_AGENT_ASSIGNED;
            case SupportChatClosedEvent ignored -> TOPIC_SUPPORT_CLOSED;
            default -> TOPIC_DEFAULT;
        };
    }

    private String extractKeyFrom(ChatEvent event) {
        return switch (event) {
            case MessageSentEvent e -> e.roomId();
            case MessageReadEvent e -> e.roomId();
            case SupportRequestCreatedEvent e -> e.roomId();
            case SupportAgentAssignedEvent e -> e.roomId();
            case SupportChatClosedEvent e -> e.roomId();
            default -> null;
        };
    }
}
