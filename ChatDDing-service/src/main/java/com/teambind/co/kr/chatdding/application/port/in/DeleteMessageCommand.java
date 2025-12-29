package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.message.MessageId;

import java.util.Objects;

/**
 * 메시지 삭제 Command DTO
 */
public record DeleteMessageCommand(
        RoomId roomId,
        MessageId messageId,
        UserId userId
) {

    public DeleteMessageCommand {
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(messageId, "messageId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
    }

    public static DeleteMessageCommand of(String roomId, String messageId, Long userId) {
        return new DeleteMessageCommand(
                RoomId.fromString(roomId),
                MessageId.fromString(messageId),
                UserId.of(userId)
        );
    }
}
