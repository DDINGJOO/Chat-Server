package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.message.MessageId;

/**
 * 읽음 처리 Command DTO
 *
 * @param roomId        채팅방 ID
 * @param userId        요청자 ID
 * @param lastMessageId 마지막으로 읽은 메시지 ID (optional, null이면 모든 메시지 읽음 처리)
 */
public record MarkAsReadCommand(
        RoomId roomId,
        UserId userId,
        MessageId lastMessageId
) {

    public MarkAsReadCommand {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }

    public static MarkAsReadCommand of(String roomId, Long userId, String lastMessageId) {
        return new MarkAsReadCommand(
                RoomId.fromString(roomId),
                UserId.of(userId),
                lastMessageId != null && !lastMessageId.isBlank()
                        ? MessageId.fromString(lastMessageId)
                        : null
        );
    }

    public static MarkAsReadCommand of(String roomId, Long userId) {
        return of(roomId, userId, null);
    }
}
