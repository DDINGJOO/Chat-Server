package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomContext;

import java.time.LocalDateTime;

/**
 * 공간 문의 생성 Result DTO
 */
public record CreatePlaceInquiryResult(
        String roomId,
        String type,
        ContextDto context,
        LocalDateTime createdAt
) {

    public record ContextDto(
            String contextType,
            Long contextId,
            String contextName
    ) {
        public static ContextDto from(ChatRoomContext context) {
            return new ContextDto(
                    context.contextType().name(),
                    context.contextId(),
                    context.contextName()
            );
        }
    }

    public static CreatePlaceInquiryResult from(ChatRoom chatRoom) {
        return new CreatePlaceInquiryResult(
                chatRoom.getId().toStringValue(),
                chatRoom.getType().name(),
                ContextDto.from(chatRoom.getContext()),
                chatRoom.getCreatedAt()
        );
    }
}
