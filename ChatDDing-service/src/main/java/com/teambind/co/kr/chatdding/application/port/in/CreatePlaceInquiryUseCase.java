package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 공간 문의 생성 UseCase
 */
public interface CreatePlaceInquiryUseCase {

    /**
     * 공간 문의 채팅방을 생성합니다.
     *
     * @param command 생성 요청 정보
     * @return 생성된 채팅방 정보
     */
    CreatePlaceInquiryResult execute(CreatePlaceInquiryCommand command);
}
