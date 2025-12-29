package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;

import java.time.LocalDateTime;

/**
 * 상담 종료 Result DTO
 */
public record CloseSupportChatResult(
        String roomId,
        LocalDateTime closedAt
) {

    public static CloseSupportChatResult from(ChatRoom chatRoom, LocalDateTime closedAt) {
        return new CloseSupportChatResult(
                chatRoom.getId().toStringValue(),
                closedAt
        );
    }
}
