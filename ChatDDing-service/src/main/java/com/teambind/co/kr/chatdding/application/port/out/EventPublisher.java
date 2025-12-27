package com.teambind.co.kr.chatdding.application.port.out;

import com.teambind.co.kr.chatdding.domain.event.ChatEvent;

/**
 * 이벤트 발행 Port (Outbound)
 *
 * <p>Hexagonal Architecture의 Outbound Port</p>
 * <p>도메인 이벤트를 외부 메시징 시스템으로 발행</p>
 */
public interface EventPublisher {

    /**
     * 이벤트 발행
     *
     * @param event 발행할 이벤트
     */
    void publish(ChatEvent event);

    /**
     * 특정 토픽으로 이벤트 발행
     *
     * @param topic 토픽명
     * @param event 발행할 이벤트
     */
    void publish(String topic, ChatEvent event);
}
