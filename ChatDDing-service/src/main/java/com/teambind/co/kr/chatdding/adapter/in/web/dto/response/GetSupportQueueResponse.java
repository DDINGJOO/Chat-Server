package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상담 대기열 조회 Response DTO
 */
public record GetSupportQueueResponse(
        List<SupportQueueItemDto> items,
        String nextCursor,
        boolean hasMore,
        long totalCount
) {

    public record SupportQueueItemDto(
            String roomId,
            Long userId,
            String category,
            LocalDateTime createdAt,
            long waitingMinutes
    ) {
        public static SupportQueueItemDto from(GetSupportQueueResult.SupportQueueItem item) {
            return new SupportQueueItemDto(
                    item.roomId(),
                    item.userId(),
                    item.category(),
                    item.createdAt(),
                    item.waitingMinutes()
            );
        }
    }

    public static GetSupportQueueResponse from(GetSupportQueueResult result) {
        return new GetSupportQueueResponse(
                result.items().stream().map(SupportQueueItemDto::from).toList(),
                result.nextCursor(),
                result.hasMore(),
                result.totalCount()
        );
    }
}
