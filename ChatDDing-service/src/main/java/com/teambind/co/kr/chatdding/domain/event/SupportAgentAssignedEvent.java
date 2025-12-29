package com.teambind.co.kr.chatdding.domain.event;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.common.UserId;

import java.time.LocalDateTime;

/**
 * 상담원 배정 이벤트
 *
 * <p>상담원이 상담 채팅방에 배정될 때 발행되는 이벤트</p>
 */
public record SupportAgentAssignedEvent(
        String roomId,
        Long userId,
        Long agentId,
        LocalDateTime occurredAt
) implements ChatEvent {

    public static final String EVENT_TYPE = "SUPPORT_AGENT_ASSIGNED";

    public static SupportAgentAssignedEvent from(ChatRoom chatRoom, UserId agentId) {
        return new SupportAgentAssignedEvent(
                chatRoom.getId().toStringValue(),
                chatRoom.getOwnerId().getValue(),
                agentId.getValue(),
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
