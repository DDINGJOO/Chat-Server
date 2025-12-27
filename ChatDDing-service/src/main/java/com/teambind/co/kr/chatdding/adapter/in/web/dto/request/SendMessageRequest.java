package com.teambind.co.kr.chatdding.adapter.in.web.dto.request;

import com.teambind.co.kr.chatdding.application.port.in.SendMessageCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 메시지 전송 API 요청 DTO
 */
public record SendMessageRequest(
        @NotBlank(message = "메시지 내용은 필수입니다")
        @Size(max = 5000, message = "메시지는 최대 5000자까지 가능합니다")
        String content
) {

    public SendMessageCommand toCommand(String roomId, Long senderId) {
        return SendMessageCommand.of(roomId, senderId, content);
    }
}
