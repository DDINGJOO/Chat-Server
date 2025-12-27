package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.message.MessageId;

/**
 * 메시지 조회 Query DTO
 *
 * @param roomId   채팅방 ID
 * @param userId   요청자 ID (권한 검증용)
 * @param cursorId 커서 메시지 ID (이 메시지 이전 것들을 조회, null이면 최신부터)
 * @param limit    조회할 메시지 수
 */
public record GetMessagesQuery(
        RoomId roomId,
        UserId userId,
        MessageId cursorId,
        int limit
) {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    public GetMessagesQuery {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
    }

    public static GetMessagesQuery of(String roomId, Long userId, String cursorId, Integer limit) {
        return new GetMessagesQuery(
                RoomId.fromString(roomId),
                UserId.of(userId),
                cursorId != null && !cursorId.isBlank() ? MessageId.fromString(cursorId) : null,
                limit != null ? limit : DEFAULT_LIMIT
        );
    }

    public boolean hasCursor() {
        return cursorId != null;
    }
}
