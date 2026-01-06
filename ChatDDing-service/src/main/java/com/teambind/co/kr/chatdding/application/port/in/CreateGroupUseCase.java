package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 그룹 채팅방 생성 UseCase
 */
public interface CreateGroupUseCase {

    /**
     * 그룹 채팅방을 생성합니다.
     *
     * @param command 생성 요청 정보
     * @return 생성된 채팅방 정보
     */
    CreateGroupResult execute(CreateGroupCommand command);
}
