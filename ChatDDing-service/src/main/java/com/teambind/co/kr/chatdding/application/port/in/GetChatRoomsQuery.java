package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;
import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * 채팅방 목록 조회 Query DTO
 *
 * @param userId 요청자 ID
 * @param type   채팅방 타입 (nullable, null이면 전체 조회)
 */
public record GetChatRoomsQuery(
        UserId userId,
        ChatRoomType type
) {

    public GetChatRoomsQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }

    public static GetChatRoomsQuery of(Long userId) {
        return new GetChatRoomsQuery(UserId.of(userId), null);
    }

    public static GetChatRoomsQuery of(Long userId, ChatRoomType type) {
        return new GetChatRoomsQuery(UserId.of(userId), type);
    }
}
