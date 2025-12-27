package com.teambind.co.kr.chatdding.domain.event;

import java.time.LocalDateTime;

/**
 * 채팅 도메인 이벤트 기본 인터페이스
 */
public interface ChatEvent {

    String getEventType();

    LocalDateTime getOccurredAt();
}
