package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 상담 요청 생성 UseCase
 */
public interface CreateSupportRequestUseCase {

    /**
     * 상담 요청 채팅방을 생성합니다.
     *
     * @param command 생성 요청 정보
     * @return 생성된 채팅방 정보
     */
    CreateSupportRequestResult execute(CreateSupportRequestCommand command);
}
