package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * 메시지 전송 Command DTO
 *
 * @param roomId   채팅방 ID
 * @param senderId 발신자 ID
 * @param content  메시지 내용
 */
public record SendMessageCommand(
        RoomId roomId,
        UserId senderId,
        String content
) {

    public SendMessageCommand {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId cannot be null");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("senderId cannot be null");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be null or blank");
        }
    }

    public static SendMessageCommand of(Long roomId, Long senderId, String content) {
        return new SendMessageCommand(
                RoomId.of(roomId),
                UserId.of(senderId),
                content
        );
    }

    public static SendMessageCommand of(String roomId, Long senderId, String content) {
        return new SendMessageCommand(
                RoomId.fromString(roomId),
                UserId.of(senderId),
                content
        );
    }
}
