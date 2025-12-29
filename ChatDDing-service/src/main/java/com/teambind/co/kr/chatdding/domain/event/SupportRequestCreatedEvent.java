package com.teambind.co.kr.chatdding.domain.event;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;

import java.time.LocalDateTime;

/**
 * 상담 요청 생성 이벤트
 *
 * <p>사용자가 고객 상담을 요청할 때 발행되는 이벤트</p>
 */
public record SupportRequestCreatedEvent(
        String roomId,
        Long userId,
        String category,
        LocalDateTime occurredAt
) implements ChatEvent {

    public static final String EVENT_TYPE = "SUPPORT_REQUEST_CREATED";

    public static SupportRequestCreatedEvent from(ChatRoom chatRoom, String category) {
        return new SupportRequestCreatedEvent(
                chatRoom.getId().toStringValue(),
                chatRoom.getOwnerId().getValue(),
                category,
                chatRoom.getCreatedAt()
        );
    }

    public static SupportRequestCreatedEvent from(ChatRoom chatRoom) {
        return from(chatRoom, null);
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
