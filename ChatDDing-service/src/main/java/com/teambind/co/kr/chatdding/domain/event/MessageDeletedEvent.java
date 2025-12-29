package com.teambind.co.kr.chatdding.domain.event;

import java.time.LocalDateTime;

/**
 * 메시지 삭제 이벤트
 */
public record MessageDeletedEvent(
        String messageId,
        String roomId,
        Long deletedBy,
        boolean hardDeleted,
        LocalDateTime occurredAt
) implements ChatEvent {

    public static final String EVENT_TYPE = "MESSAGE_DELETED";

    public static MessageDeletedEvent of(String messageId, String roomId, Long deletedBy, boolean hardDeleted) {
        return new MessageDeletedEvent(
                messageId,
                roomId,
                deletedBy,
                hardDeleted,
                LocalDateTime.now()
        );
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
