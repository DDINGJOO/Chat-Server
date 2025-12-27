package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * 공간 문의 생성 Command DTO
 *
 * @param guestId        문의자 (게스트) ID
 * @param hostId         호스트 ID
 * @param placeId        공간 ID
 * @param placeName      공간 이름
 * @param initialMessage 초기 메시지 (선택)
 */
public record CreatePlaceInquiryCommand(
        UserId guestId,
        UserId hostId,
        Long placeId,
        String placeName,
        String initialMessage
) {

    public CreatePlaceInquiryCommand {
        if (guestId == null) {
            throw new IllegalArgumentException("guestId cannot be null");
        }
        if (hostId == null) {
            throw new IllegalArgumentException("hostId cannot be null");
        }
        if (placeId == null || placeId <= 0) {
            throw new IllegalArgumentException("placeId must be positive");
        }
        if (placeName == null || placeName.isBlank()) {
            throw new IllegalArgumentException("placeName cannot be null or blank");
        }
    }

    public static CreatePlaceInquiryCommand of(Long guestId, Long hostId, Long placeId, String placeName) {
        return new CreatePlaceInquiryCommand(
                UserId.of(guestId),
                UserId.of(hostId),
                placeId,
                placeName,
                null
        );
    }

    public static CreatePlaceInquiryCommand of(Long guestId, Long hostId, Long placeId, String placeName, String initialMessage) {
        return new CreatePlaceInquiryCommand(
                UserId.of(guestId),
                UserId.of(hostId),
                placeId,
                placeName,
                initialMessage
        );
    }

    public boolean hasInitialMessage() {
        return initialMessage != null && !initialMessage.isBlank();
    }
}
