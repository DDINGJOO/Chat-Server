package com.teambind.co.kr.chatdding.adapter.in.web.dto.request;

import com.teambind.co.kr.chatdding.application.port.in.CreateGroupCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 그룹 채팅방 생성 Request DTO
 */
public record CreateGroupRequest(
        @NotEmpty(message = "memberIds is required")
        List<Long> memberIds,

        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be less than 100 characters")
        String name,

        @Size(max = 5000, message = "initialMessage must be less than 5000 characters")
        String initialMessage
) {

    public CreateGroupCommand toCommand(Long ownerId) {
        return CreateGroupCommand.of(ownerId, memberIds, name, initialMessage);
    }
}
