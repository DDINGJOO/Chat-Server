package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 상담원 배정 UseCase
 */
public interface AssignSupportAgentUseCase {

    /**
     * 상담 채팅방에 상담원을 배정합니다.
     *
     * @param command 배정 요청 정보
     * @return 배정 결과
     */
    AssignSupportAgentResult execute(AssignSupportAgentCommand command);
}
