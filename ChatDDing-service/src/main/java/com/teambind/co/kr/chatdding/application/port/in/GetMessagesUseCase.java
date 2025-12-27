package com.teambind.co.kr.chatdding.application.port.in;

import java.util.List;

/**
 * 메시지 조회 UseCase Port (Application Layer)
 *
 * <p>커서 기반 페이징으로 메시지 목록 조회</p>
 */
public interface GetMessagesUseCase {

    /**
     * 메시지 목록 조회
     *
     * @param query 조회 쿼리
     * @return 메시지 목록 결과
     */
    GetMessagesResult execute(GetMessagesQuery query);
}
