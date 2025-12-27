package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document;

import com.teambind.co.kr.chatdding.domain.chatroom.Participant;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 참여자 MongoDB Embedded Document
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDocument {

    private Long userId;
    private boolean notificationEnabled;
    private LocalDateTime lastReadAt;
    private LocalDateTime joinedAt;

    public static ParticipantDocument from(Participant participant) {
        return ParticipantDocument.builder()
                .userId(participant.getUserId().getValue())
                .notificationEnabled(participant.isNotificationEnabled())
                .lastReadAt(participant.getLastReadAt())
                .joinedAt(participant.getJoinedAt())
                .build();
    }

    public Participant toDomain() {
        return Participant.of(
                UserId.of(userId),
                notificationEnabled,
                lastReadAt,
                joinedAt
        );
    }
}
