package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.GetMessagesResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메시지 목록 조회 API 응답 DTO
 */
public record GetMessagesResponse(
        List<MessageItem> messages,
        String nextCursor,
        boolean hasMore
) {

    public static GetMessagesResponse from(GetMessagesResult result) {
        List<MessageItem> items = result.messages().stream()
                .map(m -> new MessageItem(
                        m.messageId(),
                        m.roomId(),
                        m.senderId(),
                        m.content(),
                        m.readCount(),
                        m.deleted(),
                        m.createdAt()
                ))
                .toList();

        return new GetMessagesResponse(items, result.nextCursor(), result.hasMore());
    }

    public record MessageItem(
            String messageId,
            String roomId,
            Long senderId,
            String content,
            int readCount,
            boolean deleted,
            LocalDateTime createdAt
    ) {}
}
