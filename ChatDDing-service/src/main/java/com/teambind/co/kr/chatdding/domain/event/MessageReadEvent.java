package com.teambind.co.kr.chatdding.domain.event;

import java.time.LocalDateTime;

/**
 * 메시지 읽음 이벤트
 */
public record MessageReadEvent(
        String roomId,
        Long userId,
        int readCount,
        LocalDateTime occurredAt
) implements ChatEvent {

    public static final String EVENT_TYPE = "MESSAGE_READ";

    public static MessageReadEvent of(String roomId, Long userId, int readCount) {
        return new MessageReadEvent(
                roomId,
                userId,
                readCount,
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
