package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * 상담 종료 Command DTO
 *
 * @param roomId 채팅방 ID
 * @param userId 요청자 ID (사용자 또는 상담원)
 */
public record CloseSupportChatCommand(
        RoomId roomId,
        UserId userId
) {

    public CloseSupportChatCommand {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }

    public static CloseSupportChatCommand of(String roomId, Long userId) {
        return new CloseSupportChatCommand(
                RoomId.fromString(roomId),
                UserId.of(userId)
        );
    }
}
