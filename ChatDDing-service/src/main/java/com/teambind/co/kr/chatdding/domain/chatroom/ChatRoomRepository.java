package com.teambind.co.kr.chatdding.domain.chatroom;

import com.teambind.co.kr.chatdding.domain.common.UserId;

import java.util.List;
import java.util.Optional;

/**
 * ChatRoom Repository Port (Domain Layer)
 *
 * <p>도메인 계층에서 정의하는 Repository 인터페이스</p>
 * <p>실제 구현은 Infrastructure 계층의 Adapter에서 담당</p>
 */
public interface ChatRoomRepository {

    /**
     * 채팅방 저장
     */
    ChatRoom save(ChatRoom chatRoom);

    /**
     * ID로 채팅방 조회
     */
    Optional<ChatRoom> findById(RoomId roomId);

    /**
     * 사용자가 참여중인 채팅방 목록 조회
     */
    List<ChatRoom> findByParticipantUserId(UserId userId);

    /**
     * 사용자가 참여중인 활성 채팅방 목록 조회 (최근 메시지 순)
     */
    List<ChatRoom> findActiveByParticipantUserIdOrderByLastMessageAtDesc(UserId userId);

    /**
     * DM 중복 체크용: 동일한 참여자 조합의 DM이 존재하는지 확인
     *
     * @param participantIds 정렬된 참여자 ID 목록
     */
    Optional<ChatRoom> findDmByParticipantIds(List<Long> participantIds);

    /**
     * 채팅방 존재 여부 확인
     */
    boolean existsById(RoomId roomId);

    /**
     * 채팅방 삭제
     */
    void deleteById(RoomId roomId);
}
