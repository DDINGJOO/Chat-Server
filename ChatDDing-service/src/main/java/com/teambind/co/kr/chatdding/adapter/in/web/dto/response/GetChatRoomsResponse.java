package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsResult;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 목록 조회 API 응답 DTO
 */
public record GetChatRoomsResponse(
        List<ChatRoomItem> chatRooms
) {

    public static GetChatRoomsResponse from(GetChatRoomsResult result) {
        List<ChatRoomItem> items = result.chatRooms().stream()
                .map(r -> new ChatRoomItem(
                        r.roomId(),
                        r.type(),
                        r.name(),
                        r.participantIds(),
                        r.lastMessage(),
                        r.lastMessageAt(),
                        r.unreadCount(),
                        r.context() != null ? ContextDto.from(r.context()) : null
                ))
                .toList();

        return new GetChatRoomsResponse(items);
    }

    public record ChatRoomItem(
            String roomId,
            ChatRoomType type,
            String name,
            List<Long> participantIds,
            String lastMessage,
            LocalDateTime lastMessageAt,
            long unreadCount,
            ContextDto context
    ) {}

    public record ContextDto(
            String contextType,
            Long contextId,
            String contextName
    ) {
        public static ContextDto from(GetChatRoomsResult.ContextDto ctx) {
            return new ContextDto(ctx.contextType(), ctx.contextId(), ctx.contextName());
        }
    }
}
