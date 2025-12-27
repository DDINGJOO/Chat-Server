package com.teambind.co.kr.chatdding.adapter.in.web.dto;

import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadCommand;

/**
 * 읽음 처리 API 요청 DTO
 */
public record MarkAsReadRequest(
        String lastMessageId
) {

    public MarkAsReadCommand toCommand(String roomId, Long userId) {
        return MarkAsReadCommand.of(roomId, userId, lastMessageId);
    }
}
