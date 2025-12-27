package com.teambind.co.kr.chatdding.domain.event;

import com.teambind.co.kr.chatdding.domain.message.Message;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메시지 전송 이벤트
 */
public record MessageSentEvent(
        String messageId,
        String roomId,
        Long senderId,
        String content,
        String contentPreview,
        List<Long> recipientIds,
        LocalDateTime occurredAt
) implements ChatEvent {

    public static final String EVENT_TYPE = "MESSAGE_SENT";

    public static MessageSentEvent from(Message message, List<Long> recipientIds) {
        return new MessageSentEvent(
                message.getId().toStringValue(),
                message.getRoomId().toStringValue(),
                message.getSenderId().getValue(),
                message.getContent(),
                message.getContentPreview(),
                recipientIds,
                message.getCreatedAt()
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
