package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.message.Message;

import java.time.LocalDateTime;

/**
 * 메시지 전송 결과 DTO
 *
 * @param messageId 생성된 메시지 ID (String)
 * @param roomId    채팅방 ID (String)
 * @param senderId  발신자 ID
 * @param content   메시지 내용
 * @param createdAt 생성 시간
 */
public record SendMessageResult(
        String messageId,
        String roomId,
        Long senderId,
        String content,
        LocalDateTime createdAt
) {

    public static SendMessageResult from(Message message) {
        return new SendMessageResult(
                message.getId().toStringValue(),
                message.getRoomId().toStringValue(),
                message.getSenderId().getValue(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
