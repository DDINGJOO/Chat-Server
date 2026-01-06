package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.CreateDmResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DM 채팅방 생성 Response DTO
 */
public record CreateDmResponse(
        String roomId,
        String type,
        List<Long> participantIds,
        LocalDateTime createdAt,
        boolean isNewRoom
) {

    public static CreateDmResponse from(CreateDmResult result) {
        return new CreateDmResponse(
                result.roomId(),
                result.type(),
                result.participantIds(),
                result.createdAt(),
                result.isNewRoom()
        );
    }
}
