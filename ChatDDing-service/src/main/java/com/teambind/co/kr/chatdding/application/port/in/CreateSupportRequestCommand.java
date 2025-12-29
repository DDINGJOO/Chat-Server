package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * 상담 요청 생성 Command DTO
 *
 * @param userId         요청자 ID
 * @param category       상담 카테고리 (선택)
 * @param initialMessage 초기 메시지 (선택)
 */
public record CreateSupportRequestCommand(
        UserId userId,
        String category,
        String initialMessage
) {

    public CreateSupportRequestCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }

    public static CreateSupportRequestCommand of(Long userId) {
        return new CreateSupportRequestCommand(UserId.of(userId), null, null);
    }

    public static CreateSupportRequestCommand of(Long userId, String category) {
        return new CreateSupportRequestCommand(UserId.of(userId), category, null);
    }

    public static CreateSupportRequestCommand of(Long userId, String category, String initialMessage) {
        return new CreateSupportRequestCommand(UserId.of(userId), category, initialMessage);
    }

    public boolean hasInitialMessage() {
        return initialMessage != null && !initialMessage.isBlank();
    }
}
