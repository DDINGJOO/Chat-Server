package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 상담 종료 UseCase
 */
public interface CloseSupportChatUseCase {

    /**
     * 상담 채팅을 종료합니다.
     *
     * @param command 종료 요청 정보
     * @return 종료 결과
     */
    CloseSupportChatResult execute(CloseSupportChatCommand command);
}
