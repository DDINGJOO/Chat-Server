package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;
import com.teambind.co.kr.chatdding.domain.chatroom.Participant;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 상세 조회 결과 DTO
 */
public record GetChatRoomDetailResult(
        String roomId,
        ChatRoomType type,
        String name,
        List<ParticipantInfo> participants,
        Long ownerId,
        ChatRoomStatus status,
        LocalDateTime createdAt,
        LocalDateTime lastMessageAt,
        long unreadCount
) {

    public static GetChatRoomDetailResult from(ChatRoom chatRoom, long unreadCount) {
        List<ParticipantInfo> participantInfos = chatRoom.getParticipants().stream()
                .map(ParticipantInfo::from)
                .toList();

        return new GetChatRoomDetailResult(
                chatRoom.getId().toStringValue(),
                chatRoom.getType(),
                chatRoom.getName(),
                participantInfos,
                chatRoom.getOwnerId().getValue(),
                chatRoom.getStatus(),
                chatRoom.getCreatedAt(),
                chatRoom.getLastMessageAt(),
                unreadCount
        );
    }

    public record ParticipantInfo(
            Long userId,
            boolean notificationEnabled,
            LocalDateTime lastReadAt,
            LocalDateTime joinedAt
    ) {
        public static ParticipantInfo from(Participant participant) {
            return new ParticipantInfo(
                    participant.getUserId().getValue(),
                    participant.isNotificationEnabled(),
                    participant.getLastReadAt(),
                    participant.getJoinedAt()
            );
        }
    }
}
