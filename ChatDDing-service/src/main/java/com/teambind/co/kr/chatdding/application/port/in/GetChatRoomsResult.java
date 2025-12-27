package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;
import com.teambind.co.kr.chatdding.domain.message.Message;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 목록 조회 결과 DTO
 */
public record GetChatRoomsResult(
        List<ChatRoomItem> chatRooms
) {

    public record ChatRoomItem(
            String roomId,
            ChatRoomType type,
            String name,
            List<Long> participantIds,
            String lastMessage,
            LocalDateTime lastMessageAt,
            long unreadCount
    ) {
        public static ChatRoomItem from(ChatRoom chatRoom, Message lastMessage, long unreadCount) {
            List<Long> participantIds = chatRoom.getParticipantIds().stream()
                    .map(id -> id.getValue())
                    .toList();

            return new ChatRoomItem(
                    chatRoom.getId().toStringValue(),
                    chatRoom.getType(),
                    chatRoom.getName(),
                    participantIds,
                    lastMessage != null ? lastMessage.getContentPreview() : null,
                    chatRoom.getLastMessageAt(),
                    unreadCount
            );
        }
    }
}
