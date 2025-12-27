package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * 채팅방 목록 조회 Query DTO
 *
 * @param userId 요청자 ID
 */
public record GetChatRoomsQuery(
        UserId userId
) {

    public GetChatRoomsQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }

    public static GetChatRoomsQuery of(Long userId) {
        return new GetChatRoomsQuery(UserId.of(userId));
    }
}
