package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.CreateGroupResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 그룹 채팅방 생성 Response DTO
 */
public record CreateGroupResponse(
        String roomId,
        String type,
        String name,
        Long ownerId,
        List<Long> participantIds,
        LocalDateTime createdAt
) {

    public static CreateGroupResponse from(CreateGroupResult result) {
        return new CreateGroupResponse(
                result.roomId(),
                result.type(),
                result.name(),
                result.ownerId(),
                result.participantIds(),
                result.createdAt()
        );
    }
}
