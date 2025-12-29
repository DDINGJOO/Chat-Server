package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.repository;

import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document.ChatRoomDocument;

import java.util.List;

/**
 * ChatRoom Custom Repository Interface
 *
 * <p>Spring Data MongoDB에서 자동 생성할 수 없는 커스텀 쿼리를 정의</p>
 */
public interface ChatRoomMongoRepositoryCustom {

    /**
     * 상담원 미배정 상담 대기열 조회
     *
     * @param cursor 커서 (마지막 조회 roomId, null이면 처음부터)
     * @param limit 조회 개수
     * @return 대기 중인 상담 채팅방 목록 (생성일 오름차순)
     */
    List<ChatRoomDocument> findPendingSupportRooms(String cursor, int limit);

    /**
     * 상담원 미배정 상담 대기열 수 조회
     *
     * @return 대기 중인 상담 수
     */
    long countPendingSupportRooms();
}
