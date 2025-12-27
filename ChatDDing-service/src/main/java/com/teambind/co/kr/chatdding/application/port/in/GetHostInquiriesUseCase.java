package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 호스트 문의 목록 조회 UseCase
 */
public interface GetHostInquiriesUseCase {

    /**
     * 호스트의 공간 문의 목록을 조회합니다.
     *
     * @param query 조회 조건
     * @return 문의 목록
     */
    GetHostInquiriesResult execute(GetHostInquiriesQuery query);
}
