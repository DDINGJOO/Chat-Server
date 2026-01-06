package com.teambind.co.kr.chatdding.adapter.in.web.dto.request;

import com.teambind.co.kr.chatdding.application.port.in.CreateDmCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DM 채팅방 생성 Request DTO
 */
public record CreateDmRequest(
        @NotNull(message = "recipientId is required")
        Long recipientId,

        @Size(max = 5000, message = "initialMessage must be less than 5000 characters")
        String initialMessage
) {

    public CreateDmCommand toCommand(Long senderId) {
        return CreateDmCommand.of(senderId, recipientId, initialMessage);
    }
}
