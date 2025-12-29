package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;

/**
 * 상담원 배정 Command DTO
 *
 * @param roomId  채팅방 ID
 * @param agentId 상담원 ID
 */
public record AssignSupportAgentCommand(
        RoomId roomId,
        UserId agentId
) {

    public AssignSupportAgentCommand {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId cannot be null");
        }
        if (agentId == null) {
            throw new IllegalArgumentException("agentId cannot be null");
        }
    }

    public static AssignSupportAgentCommand of(String roomId, Long agentId) {
        return new AssignSupportAgentCommand(
                RoomId.fromString(roomId),
                UserId.of(agentId)
        );
    }
}
