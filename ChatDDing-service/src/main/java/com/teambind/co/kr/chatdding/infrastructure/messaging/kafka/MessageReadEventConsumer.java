package com.teambind.co.kr.chatdding.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.event.MessageReadEvent;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 메시지 읽음 이벤트 Kafka Consumer
 *
 * <p>비동기로 MongoDB bulk update 수행</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageReadEventConsumer {

    private static final String TOPIC = "chat-message-read";
    private static final String GROUP_ID = "chatdding-message-read-group";

    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consume(String payload) {
        try {
            MessageReadEvent event = objectMapper.readValue(payload, MessageReadEvent.class);
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process MessageReadEvent: {}", payload, e);
        }
    }

    private void processEvent(MessageReadEvent event) {
        log.debug("Processing MessageReadEvent: roomId={}, userId={}",
                event.roomId(), event.userId());

        RoomId roomId = RoomId.fromString(event.roomId());
        UserId userId = UserId.of(event.userId());
        LocalDateTime readAt = event.occurredAt();

        int count = messageRepository.bulkMarkAsRead(roomId, userId, readAt);

        log.info("Bulk marked {} messages as read: roomId={}, userId={}",
                count, event.roomId(), event.userId());
    }
}
