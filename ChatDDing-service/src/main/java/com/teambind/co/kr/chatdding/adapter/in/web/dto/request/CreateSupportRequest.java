package com.teambind.co.kr.chatdding.adapter.in.web.dto.request;

import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestCommand;
import jakarta.validation.constraints.Size;

/**
 * 상담 요청 생성 Request DTO
 */
public record CreateSupportRequest(
        @Size(max = 100, message = "category must be less than 100 characters")
        String category,

        @Size(max = 5000, message = "initialMessage must be less than 5000 characters")
        String initialMessage
) {

    public CreateSupportRequestCommand toCommand(Long userId) {
        return CreateSupportRequestCommand.of(userId, category, initialMessage);
    }
}
