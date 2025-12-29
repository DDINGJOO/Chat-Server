package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatResult;

import java.time.LocalDateTime;

/**
 * 상담 종료 Response DTO
 */
public record CloseSupportResponse(
        String roomId,
        LocalDateTime closedAt
) {

    public static CloseSupportResponse from(CloseSupportChatResult result) {
        return new CloseSupportResponse(
                result.roomId(),
                result.closedAt()
        );
    }
}
