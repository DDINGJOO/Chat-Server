package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestResult;

import java.time.LocalDateTime;

/**
 * 상담 요청 생성 Response DTO
 */
public record CreateSupportResponse(
        String roomId,
        String status,
        String category,
        LocalDateTime createdAt
) {

    public static CreateSupportResponse from(CreateSupportRequestResult result) {
        return new CreateSupportResponse(
                result.roomId(),
                result.status(),
                result.category(),
                result.createdAt()
        );
    }
}
