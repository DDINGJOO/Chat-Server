package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 채팅방 상세 조회 UseCase Port
 */
public interface GetChatRoomDetailUseCase {

    /**
     * 채팅방 상세 정보 조회
     *
     * @param query 조회 쿼리
     * @return 채팅방 상세 결과
     */
    GetChatRoomDetailResult execute(GetChatRoomDetailQuery query);
}
