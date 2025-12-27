package com.teambind.co.kr.chatdding.domain.chatroom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 채팅방 컨텍스트 유형
 *
 * <p>채팅방이 어떤 도메인과 연결되어 있는지 나타냄</p>
 */
@Getter
@RequiredArgsConstructor
public enum ContextType {

    PLACE("공간 문의"),
    ORDER("주문 관련"),
    BOOKING("예약 관련");

    private final String description;
}
