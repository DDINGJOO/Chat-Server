package com.teambind.co.kr.chatdding.domain.event;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.common.UserId;

import java.time.LocalDateTime;

/**
 * 상담 종료 이벤트
 *
 * <p>상담 채팅이 종료될 때 발행되는 이벤트</p>
 */
public record SupportChatClosedEvent(
        String roomId,
        Long userId,
        Long agentId,
        LocalDateTime occurredAt
) implements ChatEvent {

    public static final String EVENT_TYPE = "SUPPORT_CHAT_CLOSED";

    public static SupportChatClosedEvent from(ChatRoom chatRoom) {
        Long agentId = chatRoom.getParticipants().stream()
                .map(p -> p.getUserId())
                .filter(id -> !id.equals(chatRoom.getOwnerId()))
                .findFirst()
                .map(UserId::getValue)
                .orElse(null);

        return new SupportChatClosedEvent(
                chatRoom.getId().toStringValue(),
                chatRoom.getOwnerId().getValue(),
                agentId,
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
