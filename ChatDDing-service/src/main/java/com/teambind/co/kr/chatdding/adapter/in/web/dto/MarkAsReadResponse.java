package com.teambind.co.kr.chatdding.adapter.in.web.dto;

import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadResult;

import java.time.LocalDateTime;

/**
 * 읽음 처리 API 응답 DTO
 */
public record MarkAsReadResponse(
        String roomId,
        Long userId,
        LocalDateTime readAt,
        int readCount
) {

    public static MarkAsReadResponse from(MarkAsReadResult result) {
        return new MarkAsReadResponse(
                result.roomId(),
                result.userId(),
                result.readAt(),
                result.readCount()
        );
    }
}
