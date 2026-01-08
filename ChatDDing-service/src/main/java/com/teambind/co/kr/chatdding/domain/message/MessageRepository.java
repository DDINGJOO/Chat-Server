package com.teambind.co.kr.chatdding.domain.message;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Message Repository Port (Domain Layer)
 *
 * <p>도메인 계층에서 정의하는 Repository 인터페이스</p>
 * <p>실제 구현은 Infrastructure 계층의 Adapter에서 담당</p>
 */
public interface MessageRepository {

    /**
     * 메시지 저장
     */
    Message save(Message message);

    /**
     * ID로 메시지 조회
     */
    Optional<Message> findById(MessageId messageId);

    /**
     * 채팅방의 메시지 목록 조회 (생성일시 내림차순, 페이징)
     *
     * @param roomId 채팅방 ID
     * @param limit  조회할 메시지 수
     * @param offset 시작 위치
     */
    List<Message> findByRoomIdOrderByCreatedAtDesc(RoomId roomId, int limit, int offset);

    /**
     * 특정 메시지 이전의 메시지 목록 조회 (커서 기반 페이징)
     *
     * @param roomId   채팅방 ID
     * @param cursorId 커서 메시지 ID (이 메시지 이전 것들을 조회)
     * @param limit    조회할 메시지 수
     */
    List<Message> findByRoomIdBeforeCursor(RoomId roomId, MessageId cursorId, int limit);

    /**
     * 특정 시간 이후의 메시지 목록 조회
     */
    List<Message> findByRoomIdAndCreatedAtAfter(RoomId roomId, LocalDateTime after);

    /**
     * 사용자가 읽지 않은 메시지 수 조회
     */
    long countUnreadByRoomIdAndUserId(RoomId roomId, UserId userId);

    /**
     * 채팅방의 마지막 메시지 조회
     */
    Optional<Message> findLatestByRoomId(RoomId roomId);

    /**
     * 메시지 삭제
     */
    void deleteById(MessageId messageId);

    /**
     * 채팅방의 모든 메시지 삭제
     */
    void deleteAllByRoomId(RoomId roomId);

    /**
     * 채팅방의 안 읽은 메시지를 일괄 읽음 처리
     *
     * @param roomId 채팅방 ID
     * @param userId 읽은 사용자 ID
     * @param readAt 읽은 시각
     * @return 업데이트된 메시지 수
     */
    int bulkMarkAsRead(RoomId roomId, UserId userId, LocalDateTime readAt);
}
