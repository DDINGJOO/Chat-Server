package com.teambind.co.kr.chatdding.application.port.in;

import java.time.LocalDateTime;

/**
 * 메시지 삭제 Result DTO
 */
public record DeleteMessageResult(
        String messageId,
        boolean hardDeleted,
        LocalDateTime deletedAt
) {

    public static DeleteMessageResult of(String messageId, boolean hardDeleted) {
        return new DeleteMessageResult(messageId, hardDeleted, LocalDateTime.now());
    }
}
