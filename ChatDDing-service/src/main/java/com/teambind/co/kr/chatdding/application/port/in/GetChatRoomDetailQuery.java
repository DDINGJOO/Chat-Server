package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * 채팅방 상세 조회 Query DTO
 *
 * @param roomId 채팅방 ID
 * @param userId 요청자 ID (권한 검증용)
 */
public record GetChatRoomDetailQuery(
        RoomId roomId,
        UserId userId
) {

    public GetChatRoomDetailQuery {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }

    public static GetChatRoomDetailQuery of(String roomId, Long userId) {
        return new GetChatRoomDetailQuery(
                RoomId.fromString(roomId),
                UserId.of(userId)
        );
    }
}
