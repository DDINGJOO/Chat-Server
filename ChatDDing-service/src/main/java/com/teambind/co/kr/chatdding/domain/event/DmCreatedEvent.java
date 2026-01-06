package com.teambind.co.kr.chatdding.domain.event;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DM 채팅방 생성 이벤트
 */
public record DmCreatedEvent(
        String roomId,
        Long senderId,
        Long recipientId,
        LocalDateTime occurredAt
) implements ChatEvent {

    public static final String EVENT_TYPE = "DM_CREATED";

    public static DmCreatedEvent from(ChatRoom chatRoom, Long senderId) {
        List<Long> participantIds = chatRoom.getSortedParticipantIdValues();
        Long recipientId = participantIds.stream()
                .filter(id -> !id.equals(senderId))
                .findFirst()
                .orElseThrow();

        return new DmCreatedEvent(
                chatRoom.getId().toStringValue(),
                senderId,
                recipientId,
                chatRoom.getCreatedAt()
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
