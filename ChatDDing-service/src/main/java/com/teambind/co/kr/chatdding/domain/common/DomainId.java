package com.teambind.co.kr.chatdding.domain.common;

/**
 * 도메인 ID의 공통 인터페이스
 *
 * <p>모든 도메인 ID는 이 인터페이스를 구현</p>
 * <p>DB에는 Long으로 저장, API 요청/응답에서는 String으로 변환</p>
 *
 * @param <T> ID 값의 타입
 */
public interface DomainId<T> {

    T getValue();

    /**
     * API 응답용 String 변환
     */
    default String toStringValue() {
        return String.valueOf(getValue());
    }
}
