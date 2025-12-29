package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.common.UserId;

import java.time.LocalDateTime;

/**
 * 상담원 배정 Result DTO
 */
public record AssignSupportAgentResult(
        String roomId,
        Long agentId,
        LocalDateTime assignedAt
) {

    public static AssignSupportAgentResult from(ChatRoom chatRoom, UserId agentId, LocalDateTime assignedAt) {
        return new AssignSupportAgentResult(
                chatRoom.getId().toStringValue(),
                agentId.getValue(),
                assignedAt
        );
    }
}
