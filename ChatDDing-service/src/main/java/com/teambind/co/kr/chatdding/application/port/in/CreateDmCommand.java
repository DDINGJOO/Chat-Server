package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * DM 채팅방 생성 Command DTO
 *
 * @param senderId    발신자 (DM 생성 요청자) ID
 * @param recipientId 수신자 ID
 * @param initialMessage 초기 메시지 (선택)
 */
public record CreateDmCommand(
        UserId senderId,
        UserId recipientId,
        String initialMessage
) {

    public CreateDmCommand {
        if (senderId == null) {
            throw new IllegalArgumentException("senderId cannot be null");
        }
        if (recipientId == null) {
            throw new IllegalArgumentException("recipientId cannot be null");
        }
        if (senderId.equals(recipientId)) {
            throw new IllegalArgumentException("Cannot create DM with yourself");
        }
    }

    public static CreateDmCommand of(Long senderId, Long recipientId) {
        return new CreateDmCommand(
                UserId.of(senderId),
                UserId.of(recipientId),
                null
        );
    }

    public static CreateDmCommand of(Long senderId, Long recipientId, String initialMessage) {
        return new CreateDmCommand(
                UserId.of(senderId),
                UserId.of(recipientId),
                initialMessage
        );
    }

    public boolean hasInitialMessage() {
        return initialMessage != null && !initialMessage.isBlank();
    }
}
