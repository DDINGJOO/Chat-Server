package com.teambind.co.kr.chatdding.adapter.in.web.dto;

import com.teambind.co.kr.chatdding.application.port.in.SendMessageResult;

import java.time.LocalDateTime;

/**
 * 메시지 전송 API 응답 DTO
 */
public record SendMessageResponse(
        String messageId,
        String roomId,
        Long senderId,
        String content,
        LocalDateTime createdAt
) {

    public static SendMessageResponse from(SendMessageResult result) {
        return new SendMessageResponse(
                result.messageId(),
                result.roomId(),
                result.senderId(),
                result.content(),
                result.createdAt()
        );
    }
}
