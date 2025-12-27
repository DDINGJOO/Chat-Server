package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 메시지 전송 UseCase Port (Application Layer)
 *
 * <p>Hexagonal Architecture의 Inbound Port</p>
 * <p>외부(Controller, WebSocket 등)에서 메시지 전송을 요청할 때 사용</p>
 */
public interface SendMessageUseCase {

    /**
     * 메시지 전송
     *
     * @param command 메시지 전송 명령
     * @return 전송 결과
     */
    SendMessageResult execute(SendMessageCommand command);
}
