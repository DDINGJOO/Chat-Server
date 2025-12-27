package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 읽음 처리 UseCase Port
 */
public interface MarkAsReadUseCase {

    /**
     * 채팅방 메시지 읽음 처리
     *
     * @param command 읽음 처리 명령
     * @return 읽음 처리 결과
     */
    MarkAsReadResult execute(MarkAsReadCommand command);
}
