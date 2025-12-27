package com.teambind.co.kr.chatdding.domain.chatroom;

import com.teambind.co.kr.chatdding.domain.common.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 채팅방 참여자 Value Object
 *
 * <p>채팅방 내 참여자의 설정 및 상태를 관리</p>
 */
@Getter
public class Participant {

    private final UserId userId;
    private boolean notificationEnabled;
    private LocalDateTime lastReadAt;
    private final LocalDateTime joinedAt;

    private Participant(UserId userId, boolean notificationEnabled,
                        LocalDateTime lastReadAt, LocalDateTime joinedAt) {
        this.userId = userId;
        this.notificationEnabled = notificationEnabled;
        this.lastReadAt = lastReadAt;
        this.joinedAt = joinedAt;
    }

    public static Participant create(UserId userId) {
        LocalDateTime now = LocalDateTime.now();
        return new Participant(userId, true, now, now);
    }

    public static Participant of(UserId userId, boolean notificationEnabled,
                                  LocalDateTime lastReadAt, LocalDateTime joinedAt) {
        return new Participant(userId, notificationEnabled, lastReadAt, joinedAt);
    }

    public void updateLastReadAt(LocalDateTime readAt) {
        if (readAt != null && (this.lastReadAt == null || readAt.isAfter(this.lastReadAt))) {
            this.lastReadAt = readAt;
        }
    }

    public void enableNotification() {
        this.notificationEnabled = true;
    }

    public void disableNotification() {
        this.notificationEnabled = false;
    }

    public boolean hasUnreadMessages(LocalDateTime lastMessageAt) {
        if (lastMessageAt == null) {
            return false;
        }
        return lastReadAt == null || lastMessageAt.isAfter(lastReadAt);
    }
}
