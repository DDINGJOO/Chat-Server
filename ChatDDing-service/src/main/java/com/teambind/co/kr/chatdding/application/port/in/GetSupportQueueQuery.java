package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 상담 대기열 조회 Query DTO
 *
 * @param cursor 페이지네이션 커서 (마지막 roomId)
 * @param limit  조회 개수
 */
public record GetSupportQueueQuery(
        String cursor,
        int limit
) {

    public GetSupportQueueQuery {
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }
    }

    public static GetSupportQueueQuery of(int limit) {
        return new GetSupportQueueQuery(null, limit);
    }

    public static GetSupportQueueQuery of(String cursor, int limit) {
        return new GetSupportQueueQuery(cursor, limit);
    }

    public boolean hasCursor() {
        return cursor != null && !cursor.isBlank();
    }
}
