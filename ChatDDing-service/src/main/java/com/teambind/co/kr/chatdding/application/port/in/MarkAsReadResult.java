package com.teambind.co.kr.chatdding.application.port.in;

import java.time.LocalDateTime;

/**
 * 읽음 처리 결과 DTO
 *
 * @param roomId       채팅방 ID
 * @param userId       사용자 ID
 * @param readAt       읽음 처리 시간
 * @param readCount    읽음 처리된 메시지 수
 */
public record MarkAsReadResult(
        String roomId,
        Long userId,
        LocalDateTime readAt,
        int readCount
) {

    public static MarkAsReadResult of(String roomId, Long userId, LocalDateTime readAt, int readCount) {
        return new MarkAsReadResult(roomId, userId, readAt, readCount);
    }
}
