package com.teambind.co.kr.chatdding.domain.event;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomContext;

import java.time.LocalDateTime;

/**
 * 공간 문의 생성 이벤트
 */
public record InquiryCreatedEvent(
        String roomId,
        Long guestId,
        Long hostId,
        ContextInfo context,
        LocalDateTime occurredAt
) implements ChatEvent {

    public static final String EVENT_TYPE = "INQUIRY_CREATED";

    public record ContextInfo(
            String contextType,
            Long contextId,
            String contextName
    ) {
        public static ContextInfo from(ChatRoomContext context) {
            return new ContextInfo(
                    context.contextType().name(),
                    context.contextId(),
                    context.contextName()
            );
        }
    }

    public static InquiryCreatedEvent from(ChatRoom chatRoom) {
        return new InquiryCreatedEvent(
                chatRoom.getId().toStringValue(),
                chatRoom.getParticipantIds().stream()
                        .filter(userId -> !userId.equals(chatRoom.getOwnerId()))
                        .findFirst()
                        .orElseThrow()
                        .getValue(),
                chatRoom.getOwnerId().getValue(),
                ContextInfo.from(chatRoom.getContext()),
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
