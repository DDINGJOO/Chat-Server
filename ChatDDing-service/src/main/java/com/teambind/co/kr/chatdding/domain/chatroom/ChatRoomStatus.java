package com.teambind.co.kr.chatdding.domain.chatroom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 채팅방 상태
 */
@Getter
@RequiredArgsConstructor
public enum ChatRoomStatus {

    ACTIVE("활성"),
    CLOSED("종료");

    private final String description;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
