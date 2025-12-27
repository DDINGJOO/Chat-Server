package com.teambind.co.kr.chatdding.domain.common;

/**
 * 사용자 ID Value Object
 *
 * <p>다른 서비스(Auth, Profile)에서 발급된 사용자 ID를 래핑</p>
 *
 * @param value 사용자 ID 값 (양수)
 */
public record UserId(Long value) implements DomainId<Long> {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId value cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("UserId value must be positive: " + value);
        }
    }

    public static UserId of(Long value) {
        return new UserId(value);
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
