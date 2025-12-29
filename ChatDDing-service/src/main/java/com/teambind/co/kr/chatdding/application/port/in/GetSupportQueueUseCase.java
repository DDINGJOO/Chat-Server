package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 상담 대기열 조회 UseCase
 */
public interface GetSupportQueueUseCase {

    /**
     * 상담원 배정을 대기 중인 상담 목록을 조회합니다.
     *
     * @param query 조회 조건
     * @return 대기 중인 상담 목록
     */
    GetSupportQueueResult execute(GetSupportQueueQuery query);
}
