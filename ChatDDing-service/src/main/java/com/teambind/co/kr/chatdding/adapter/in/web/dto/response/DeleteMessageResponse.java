package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.DeleteMessageResult;

import java.time.LocalDateTime;

/**
 * 메시지 삭제 Response DTO
 */
public record DeleteMessageResponse(
        String messageId,
        boolean hardDeleted,
        LocalDateTime deletedAt
) {

    public static DeleteMessageResponse from(DeleteMessageResult result) {
        return new DeleteMessageResponse(
                result.messageId(),
                result.hardDeleted(),
                result.deletedAt()
        );
    }
}
