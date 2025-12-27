package com.teambind.co.kr.chatdding.domain.chatroom;

import com.teambind.co.kr.chatdding.domain.common.DomainId;

/**
 * 채팅방 ID Value Object
 *
 * <p>Snowflake ID 기반, DB에는 Long 저장, API에서는 String으로 변환</p>
 *
 * @param value 채팅방 ID 값 (Snowflake Long)
 */
public record RoomId(Long value) implements DomainId<Long> {

    public RoomId {
        if (value == null) {
            throw new IllegalArgumentException("RoomId value cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("RoomId value must be positive: " + value);
        }
    }

    public static RoomId of(Long value) {
        return new RoomId(value);
    }

    /**
     * String에서 RoomId 생성 (API 요청 파싱용)
     */
    public static RoomId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("RoomId string value cannot be null or blank");
        }
        return new RoomId(Long.parseLong(value));
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
