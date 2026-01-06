package com.teambind.co.kr.chatdding.domain.event;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 그룹 채팅방 생성 이벤트
 */
public record GroupCreatedEvent(
        String roomId,
        String name,
        Long ownerId,
        List<Long> participantIds,
        LocalDateTime occurredAt
) implements ChatEvent {

    public static final String EVENT_TYPE = "GROUP_CREATED";

    public static GroupCreatedEvent from(ChatRoom chatRoom) {
        return new GroupCreatedEvent(
                chatRoom.getId().toStringValue(),
                chatRoom.getName(),
                chatRoom.getOwnerId().getValue(),
                chatRoom.getSortedParticipantIdValues(),
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
