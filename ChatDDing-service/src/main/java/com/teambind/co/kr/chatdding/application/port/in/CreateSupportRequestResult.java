package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;

import java.time.LocalDateTime;

/**
 * 상담 요청 생성 Result DTO
 */
public record CreateSupportRequestResult(
        String roomId,
        String status,
        String category,
        LocalDateTime createdAt
) {

    public static CreateSupportRequestResult from(ChatRoom chatRoom, String category) {
        return new CreateSupportRequestResult(
                chatRoom.getId().toStringValue(),
                chatRoom.getStatus().name(),
                category,
                chatRoom.getCreatedAt()
        );
    }
}
