package com.teambind.co.kr.chatdding.domain.chatroom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 채팅방 유형
 */
@Getter
@RequiredArgsConstructor
public enum ChatRoomType {

    DM("1:1 개인 대화", 2),
    GROUP("그룹 채팅", 100),
    PLACE_INQUIRY("공간 문의", 2),
    SUPPORT("고객 상담", 2);

    private final String description;
    private final int maxParticipants;

    /**
     * 컨텍스트가 필요한 채팅방 유형인지 확인
     */
    public boolean requiresContext() {
        return this == PLACE_INQUIRY;
    }

    public boolean canAddParticipant(int currentCount) {
        return currentCount < maxParticipants;
    }
}
