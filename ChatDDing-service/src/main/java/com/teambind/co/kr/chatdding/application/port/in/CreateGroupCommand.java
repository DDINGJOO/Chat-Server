package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.common.UserId;

import java.util.List;

/**
 * 그룹 채팅방 생성 Command DTO
 *
 * @param ownerId   방장 ID
 * @param memberIds 멤버 ID 목록 (방장 제외)
 * @param name      채팅방 이름
 * @param initialMessage 초기 메시지 (선택)
 */
public record CreateGroupCommand(
        UserId ownerId,
        List<UserId> memberIds,
        String name,
        String initialMessage
) {

    public CreateGroupCommand {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId cannot be null");
        }
        if (memberIds == null || memberIds.isEmpty()) {
            throw new IllegalArgumentException("memberIds cannot be null or empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
        if (memberIds.contains(ownerId)) {
            throw new IllegalArgumentException("memberIds should not contain ownerId");
        }
    }

    public static CreateGroupCommand of(Long ownerId, List<Long> memberIds, String name) {
        return new CreateGroupCommand(
                UserId.of(ownerId),
                memberIds.stream().map(UserId::of).toList(),
                name,
                null
        );
    }

    public static CreateGroupCommand of(Long ownerId, List<Long> memberIds, String name, String initialMessage) {
        return new CreateGroupCommand(
                UserId.of(ownerId),
                memberIds.stream().map(UserId::of).toList(),
                name,
                initialMessage
        );
    }

    public boolean hasInitialMessage() {
        return initialMessage != null && !initialMessage.isBlank();
    }
}
