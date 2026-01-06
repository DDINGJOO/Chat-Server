package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DM 채팅방 생성 Result DTO
 */
public record CreateDmResult(
        String roomId,
        String type,
        List<Long> participantIds,
        LocalDateTime createdAt,
        boolean isNewRoom
) {

    public static CreateDmResult from(ChatRoom chatRoom, boolean isNewRoom) {
        return new CreateDmResult(
                chatRoom.getId().toStringValue(),
                chatRoom.getType().name(),
                chatRoom.getSortedParticipantIdValues(),
                chatRoom.getCreatedAt(),
                isNewRoom
        );
    }
}
