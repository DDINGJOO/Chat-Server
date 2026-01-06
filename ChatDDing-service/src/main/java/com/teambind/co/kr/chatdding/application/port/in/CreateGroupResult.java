package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 그룹 채팅방 생성 Result DTO
 */
public record CreateGroupResult(
        String roomId,
        String type,
        String name,
        Long ownerId,
        List<Long> participantIds,
        LocalDateTime createdAt
) {

    public static CreateGroupResult from(ChatRoom chatRoom) {
        return new CreateGroupResult(
                chatRoom.getId().toStringValue(),
                chatRoom.getType().name(),
                chatRoom.getName(),
                chatRoom.getOwnerId().getValue(),
                chatRoom.getSortedParticipantIdValues(),
                chatRoom.getCreatedAt()
        );
    }
}
