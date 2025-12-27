package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 채팅방 목록 조회 UseCase Port
 */
public interface GetChatRoomsUseCase {

    /**
     * 사용자의 채팅방 목록 조회
     *
     * @param query 조회 쿼리
     * @return 채팅방 목록 결과
     */
    GetChatRoomsResult execute(GetChatRoomsQuery query);
}
