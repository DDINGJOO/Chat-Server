package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.AssignSupportAgentResult;

import java.time.LocalDateTime;

/**
 * 상담원 배정 Response DTO
 */
public record AssignAgentResponse(
        String roomId,
        Long agentId,
        LocalDateTime assignedAt
) {

    public static AssignAgentResponse from(AssignSupportAgentResult result) {
        return new AssignAgentResponse(
                result.roomId(),
                result.agentId(),
                result.assignedAt()
        );
    }
}
