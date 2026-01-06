package com.teambind.co.kr.chatdding.application.port.in;

/**
 * DM 채팅방 생성 UseCase
 */
public interface CreateDmUseCase {

    /**
     * DM 채팅방을 생성합니다.
     * 이미 동일한 참여자 조합의 DM이 존재하면 기존 채팅방을 반환합니다.
     *
     * @param command 생성 요청 정보
     * @return 생성된 또는 기존 채팅방 정보
     */
    CreateDmResult execute(CreateDmCommand command);
}
