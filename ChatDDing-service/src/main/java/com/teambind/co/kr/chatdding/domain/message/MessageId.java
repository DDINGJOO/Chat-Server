package com.teambind.co.kr.chatdding.domain.message;

import com.teambind.co.kr.chatdding.domain.common.DomainId;

/**
 * 메시지 ID Value Object
 *
 * <p>Snowflake ID 기반, DB에는 Long 저장, API에서는 String으로 변환</p>
 *
 * @param value 메시지 ID 값 (Snowflake Long)
 */
public record MessageId(Long value) implements DomainId<Long> {

    public MessageId {
        if (value == null) {
            throw new IllegalArgumentException("MessageId value cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("MessageId value must be positive: " + value);
        }
    }

    public static MessageId of(Long value) {
        return new MessageId(value);
    }

    /**
     * String에서 MessageId 생성 (API 요청 파싱용)
     */
    public static MessageId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MessageId string value cannot be null or blank");
        }
        return new MessageId(Long.parseLong(value));
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
