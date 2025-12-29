package com.teambind.co.kr.chatdding.application.port.out;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 안읽은 메시지 수 캐시 Port (Outbound)
 *
 * <p>Hexagonal Architecture의 Outbound Port</p>
 * <p>Redis 캐시를 통한 안읽은 메시지 수 관리</p>
 */
public interface UnreadCountCachePort {

    /**
     * 안읽은 메시지 수 조회
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 캐시된 안읽은 메시지 수 (캐시 미스 시 Optional.empty())
     */
    Optional<Integer> getUnreadCount(RoomId roomId, UserId userId);

    /**
     * 안읽은 메시지 수 설정
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @param count  안읽은 메시지 수
     */
    void setUnreadCount(RoomId roomId, UserId userId, int count);

    /**
     * 안읽은 메시지 수 증가 (+1)
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    void incrementUnreadCount(RoomId roomId, UserId userId);

    /**
     * 안읽은 메시지 수 초기화 (읽음 처리)
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    void resetUnreadCount(RoomId roomId, UserId userId);

    /**
     * 여러 채팅방의 안읽은 메시지 수 일괄 조회
     *
     * @param roomIds 채팅방 ID 목록
     * @param userId  사용자 ID
     * @return 채팅방 ID와 안읽은 메시지 수 맵 (캐시 히트된 항목만 포함)
     */
    Map<RoomId, Integer> getUnreadCounts(List<RoomId> roomIds, UserId userId);
}
