package com.teambind.co.kr.chatdding.domain.message;

import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 메시지 Aggregate Root
 *
 * <p>메시지의 생성, 읽음 처리, 삭제 등 핵심 비즈니스 로직을 캡슐화</p>
 */
@Getter
public class Message {

    private static final int MAX_CONTENT_LENGTH = 5000;

    private final MessageId id;
    private final RoomId roomId;
    private final UserId senderId;
    private final String content;
    private final Map<UserId, LocalDateTime> readBy;
    private final Set<UserId> deletedBy;
    private final LocalDateTime createdAt;

    private Message(MessageId id, RoomId roomId, UserId senderId, String content,
                    Map<UserId, LocalDateTime> readBy, Set<UserId> deletedBy,
                    LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.readBy = new HashMap<>(readBy);
        this.deletedBy = new HashSet<>(deletedBy);
        this.createdAt = createdAt;
    }

    /**
     * 새 메시지 생성
     *
     * @param id       Snowflake로 생성된 MessageId (Application Layer에서 주입)
     * @param roomId   채팅방 ID
     * @param senderId 발신자 ID
     * @param content  메시지 내용
     */
    public static Message create(MessageId id, RoomId roomId, UserId senderId, String content) {
        validateContent(content);

        Map<UserId, LocalDateTime> readBy = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        readBy.put(senderId, now);

        return new Message(
                id,
                roomId,
                senderId,
                content,
                readBy,
                new HashSet<>(),
                now
        );
    }

    /**
     * 기존 데이터로부터 Message 복원 (Repository용)
     */
    public static Message restore(MessageId id, RoomId roomId, UserId senderId, String content,
                                   Map<UserId, LocalDateTime> readBy, Set<UserId> deletedBy,
                                   LocalDateTime createdAt) {
        return new Message(id, roomId, senderId, content, readBy, deletedBy, createdAt);
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw ChatException.of(ErrorCode.MESSAGE_CONTENT_EMPTY);
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw ChatException.of(ErrorCode.MESSAGE_CONTENT_TOO_LONG);
        }
    }

    /**
     * 읽음 처리
     */
    public void markAsRead(UserId userId) {
        if (!readBy.containsKey(userId)) {
            readBy.put(userId, LocalDateTime.now());
        }
    }

    /**
     * 특정 시간으로 읽음 처리 (배치 처리용)
     */
    public void markAsReadAt(UserId userId, LocalDateTime readAt) {
        if (!readBy.containsKey(userId)) {
            readBy.put(userId, readAt);
        }
    }

    /**
     * 메시지 삭제 (사용자별 소프트 삭제)
     */
    public void deleteFor(UserId userId) {
        deletedBy.add(userId);
    }

    /**
     * 특정 사용자가 이 메시지를 읽었는지 확인
     */
    public boolean isReadBy(UserId userId) {
        return readBy.containsKey(userId);
    }

    /**
     * 특정 사용자에게 삭제된 메시지인지 확인
     */
    public boolean isDeletedFor(UserId userId) {
        return deletedBy.contains(userId);
    }

    /**
     * 특정 사용자에게 보여줄 수 있는 메시지인지 확인
     */
    public boolean isVisibleTo(UserId userId) {
        return !isDeletedFor(userId);
    }

    /**
     * 읽음 상태 맵 (불변)
     */
    public Map<UserId, LocalDateTime> getReadBy() {
        return Collections.unmodifiableMap(readBy);
    }

    /**
     * 삭제한 사용자 목록 (불변)
     */
    public Set<UserId> getDeletedBy() {
        return Collections.unmodifiableSet(deletedBy);
    }

    /**
     * 메시지 미리보기 (알림용)
     */
    public String getContentPreview() {
        if (content.length() <= 50) {
            return content;
        }
        return content.substring(0, 50) + "...";
    }

    /**
     * 읽은 사용자 수
     */
    public int getReadCount() {
        return readBy.size();
    }

    /**
     * 삭제한 사용자 수
     */
    public int getDeletedByCount() {
        return deletedBy.size();
    }

    /**
     * 모든 참여자가 삭제했는지 확인 (Hard Delete 조건)
     *
     * @param participantCount 채팅방 참여자 수
     * @return true면 물리적 삭제 필요
     */
    public boolean shouldHardDelete(int participantCount) {
        return deletedBy.size() >= participantCount;
    }
}
