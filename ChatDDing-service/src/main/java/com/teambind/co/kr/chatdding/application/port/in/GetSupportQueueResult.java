package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 상담 대기열 조회 Result DTO
 */
public record GetSupportQueueResult(
        List<SupportQueueItem> items,
        String nextCursor,
        long totalCount
) {

    public record SupportQueueItem(
            String roomId,
            Long userId,
            String category,
            LocalDateTime createdAt,
            long waitingMinutes
    ) {
        public static SupportQueueItem from(ChatRoom chatRoom, String category) {
            long waitingMinutes = Duration.between(chatRoom.getCreatedAt(), LocalDateTime.now()).toMinutes();
            return new SupportQueueItem(
                    chatRoom.getId().toStringValue(),
                    chatRoom.getOwnerId().getValue(),
                    category,
                    chatRoom.getCreatedAt(),
                    waitingMinutes
            );
        }
    }

    public static GetSupportQueueResult of(List<SupportQueueItem> items, String nextCursor, long totalCount) {
        return new GetSupportQueueResult(items, nextCursor, totalCount);
    }

    public boolean hasMore() {
        return nextCursor != null;
    }
}
