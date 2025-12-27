package com.teambind.co.kr.chatdding.adapter.in.web.dto;

import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailResult;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 상세 조회 API 응답 DTO
 */
public record GetChatRoomDetailResponse(
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

    public static GetChatRoomDetailResponse from(GetChatRoomDetailResult result) {
        List<ParticipantInfo> participants = result.participants().stream()
                .map(p -> new ParticipantInfo(
                        p.userId(),
                        p.notificationEnabled(),
                        p.lastReadAt(),
                        p.joinedAt()
                ))
                .toList();

        return new GetChatRoomDetailResponse(
                result.roomId(),
                result.type(),
                result.name(),
                participants,
                result.ownerId(),
                result.status(),
                result.createdAt(),
                result.lastMessageAt(),
                result.unreadCount()
        );
    }

    public record ParticipantInfo(
            Long userId,
            boolean notificationEnabled,
            LocalDateTime lastReadAt,
            LocalDateTime joinedAt
    ) {}
}
