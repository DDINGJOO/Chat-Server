package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.message.Message;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메시지 조회 결과 DTO
 */
public record GetMessagesResult(
        List<MessageItem> messages,
        String nextCursor,
        boolean hasMore
) {

    public static GetMessagesResult of(List<Message> messages, int requestedLimit) {
        List<MessageItem> items = messages.stream()
                .map(MessageItem::from)
                .toList();

        boolean hasMore = messages.size() >= requestedLimit;
        String nextCursor = hasMore && !messages.isEmpty()
                ? messages.get(messages.size() - 1).getId().toStringValue()
                : null;

        return new GetMessagesResult(items, nextCursor, hasMore);
    }

    public record MessageItem(
            String messageId,
            String roomId,
            Long senderId,
            String content,
            int readCount,
            LocalDateTime createdAt
    ) {
        public static MessageItem from(Message message) {
            return new MessageItem(
                    message.getId().toStringValue(),
                    message.getRoomId().toStringValue(),
                    message.getSenderId().getValue(),
                    message.getContent(),
                    message.getReadCount(),
                    message.getCreatedAt()
            );
        }
    }
}
