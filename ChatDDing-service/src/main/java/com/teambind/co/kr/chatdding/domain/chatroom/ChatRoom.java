package com.teambind.co.kr.chatdding.domain.chatroom;

import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 채팅방 Aggregate Root
 *
 * <p>채팅방의 생성, 참여자 관리, 상태 변경 등 핵심 비즈니스 로직을 캡슐화</p>
 */
@Getter
public class ChatRoom {

    private final RoomId id;
    private final ChatRoomType type;
    private String name;
    private final List<Participant> participants;
    private final UserId ownerId;
    private ChatRoomStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    private ChatRoom(RoomId id, ChatRoomType type, String name,
                     List<Participant> participants, UserId ownerId,
                     ChatRoomStatus status, LocalDateTime createdAt,
                     LocalDateTime lastMessageAt) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.participants = new ArrayList<>(participants);
        this.ownerId = ownerId;
        this.status = status;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
    }

    /**
     * DM 채팅방 생성
     *
     * @param id          Snowflake로 생성된 RoomId (Application Layer에서 주입)
     * @param senderId    발신자 ID
     * @param recipientId 수신자 ID
     */
    public static ChatRoom createDm(RoomId id, UserId senderId, UserId recipientId) {
        List<Participant> participants = List.of(
                Participant.create(senderId),
                Participant.create(recipientId)
        );

        LocalDateTime now = LocalDateTime.now();
        return new ChatRoom(
                id,
                ChatRoomType.DM,
                null,
                participants,
                senderId,
                ChatRoomStatus.ACTIVE,
                now,
                now
        );
    }

    /**
     * 그룹 채팅방 생성
     *
     * @param id        Snowflake로 생성된 RoomId (Application Layer에서 주입)
     * @param ownerId   방장 ID
     * @param memberIds 멤버 ID 목록
     * @param name      채팅방 이름
     */
    public static ChatRoom createGroup(RoomId id, UserId ownerId, List<UserId> memberIds, String name) {
        if (memberIds.size() + 1 > ChatRoomType.GROUP.getMaxParticipants()) {
            throw ChatException.of(ErrorCode.GROUP_MAX_PARTICIPANTS_EXCEEDED);
        }

        List<Participant> participants = new ArrayList<>();
        participants.add(Participant.create(ownerId));
        memberIds.forEach(memberId -> participants.add(Participant.create(memberId)));

        LocalDateTime now = LocalDateTime.now();
        return new ChatRoom(
                id,
                ChatRoomType.GROUP,
                name,
                participants,
                ownerId,
                ChatRoomStatus.ACTIVE,
                now,
                now
        );
    }

    /**
     * 고객 상담 채팅방 생성
     *
     * @param id     Snowflake로 생성된 RoomId (Application Layer에서 주입)
     * @param userId 사용자 ID
     */
    public static ChatRoom createSupport(RoomId id, UserId userId) {
        List<Participant> participants = List.of(Participant.create(userId));

        LocalDateTime now = LocalDateTime.now();
        return new ChatRoom(
                id,
                ChatRoomType.SUPPORT,
                null,
                participants,
                userId,
                ChatRoomStatus.ACTIVE,
                now,
                now
        );
    }

    /**
     * 기존 데이터로부터 ChatRoom 복원 (Repository용)
     */
    public static ChatRoom restore(RoomId id, ChatRoomType type, String name,
                                    List<Participant> participants, UserId ownerId,
                                    ChatRoomStatus status, LocalDateTime createdAt,
                                    LocalDateTime lastMessageAt) {
        return new ChatRoom(id, type, name, participants, ownerId, status, createdAt, lastMessageAt);
    }

    /**
     * 참여자 목록 (불변)
     */
    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    /**
     * 참여자 ID 목록
     */
    public List<UserId> getParticipantIds() {
        return participants.stream()
                .map(Participant::getUserId)
                .toList();
    }

    /**
     * 정렬된 참여자 ID 목록 (DM 중복 체크용)
     */
    public List<Long> getSortedParticipantIdValues() {
        return getParticipantIds().stream()
                .map(UserId::getValue)
                .sorted()
                .toList();
    }

    /**
     * 참여자 여부 확인
     */
    public boolean isParticipant(UserId userId) {
        return participants.stream()
                .anyMatch(p -> p.getUserId().equals(userId));
    }

    /**
     * 참여자 조회
     */
    public Optional<Participant> findParticipant(UserId userId) {
        return participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * 마지막 메시지 시간 업데이트
     */
    public void updateLastMessageAt(LocalDateTime messageCreatedAt) {
        if (messageCreatedAt != null) {
            this.lastMessageAt = messageCreatedAt;
        }
    }

    /**
     * 채팅방 이름 변경 (GROUP만 가능)
     */
    public void changeName(String newName) {
        if (type != ChatRoomType.GROUP) {
            throw new IllegalStateException("Only GROUP chat room can change name");
        }
        this.name = newName;
    }

    /**
     * 상담원 배정 (SUPPORT만 가능)
     */
    public void assignAgent(UserId agentId) {
        if (type != ChatRoomType.SUPPORT) {
            throw new IllegalStateException("Only SUPPORT chat room can assign agent");
        }
        if (participants.size() >= ChatRoomType.SUPPORT.getMaxParticipants()) {
            throw new IllegalStateException("Agent already assigned");
        }
        participants.add(Participant.create(agentId));
    }

    /**
     * 채팅방 종료
     */
    public void close() {
        this.status = ChatRoomStatus.CLOSED;
    }

    /**
     * 활성 상태 여부
     */
    public boolean isActive() {
        return status.isActive();
    }
}
