package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * 호스트 문의 목록 조회 Query DTO
 *
 * @param hostId  호스트 ID
 * @param placeId 공간 ID (선택, 필터링용)
 * @param cursor  페이지네이션 커서 (선택)
 * @param limit   조회 개수
 */
public record GetHostInquiriesQuery(
        UserId hostId,
        Long placeId,
        String cursor,
        int limit
) {

    public GetHostInquiriesQuery {
        if (hostId == null) {
            throw new IllegalArgumentException("hostId cannot be null");
        }
        if (limit <= 0 || limit > 100) {
            limit = 20;
        }
    }

    public static GetHostInquiriesQuery of(Long hostId) {
        return new GetHostInquiriesQuery(UserId.of(hostId), null, null, 20);
    }

    public static GetHostInquiriesQuery of(Long hostId, Long placeId) {
        return new GetHostInquiriesQuery(UserId.of(hostId), placeId, null, 20);
    }

    public static GetHostInquiriesQuery of(Long hostId, Long placeId, String cursor, int limit) {
        return new GetHostInquiriesQuery(UserId.of(hostId), placeId, cursor, limit);
    }

    public boolean hasPlaceIdFilter() {
        return placeId != null;
    }

    public boolean hasCursor() {
        return cursor != null && !cursor.isBlank();
    }
}
