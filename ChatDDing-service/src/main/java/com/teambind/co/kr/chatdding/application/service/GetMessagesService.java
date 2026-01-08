package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.GetMessagesQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetMessagesResult;
import com.teambind.co.kr.chatdding.application.port.in.GetMessagesUseCase;
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher;
import com.teambind.co.kr.chatdding.application.port.out.UnreadCountCachePort;
import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.event.MessageReadEvent;
import com.teambind.co.kr.chatdding.domain.message.Message;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 메시지 조회 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMessagesService implements GetMessagesUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UnreadCountCachePort unreadCountCachePort;
    private final EventPublisher eventPublisher;

    @Override
    public GetMessagesResult execute(GetMessagesQuery query) {
        validateAccess(query);

        List<Message> messages = fetchMessages(query);

        // 자동 읽음 처리 트리거 (최적화 포함)
        triggerAutoReadMarking(query.roomId(), query.userId());

        return GetMessagesResult.of(messages, query.userId(), query.limit());
    }

    /**
     * 자동 읽음 처리 트리거
     *
     * <p>캐시에서 unreadCount를 확인하여 0이면 스킵 (polling 최적화)</p>
     * <p>0이 아니면 캐시 리셋 후 이벤트 발행 (비동기 DB 업데이트)</p>
     */
    private void triggerAutoReadMarking(RoomId roomId, UserId userId) {
        // 캐시에서 unreadCount 확인 - 0이면 스킵 (polling 최적화)
        Optional<Integer> cached = unreadCountCachePort.getUnreadCount(roomId, userId);
        if (cached.isPresent() && cached.get() == 0) {
            return;
        }

        // 1. 캐시 즉시 리셋 (동기)
        unreadCountCachePort.resetUnreadCount(roomId, userId);

        // 2. 이벤트 발행 (비동기 DB 업데이트용)
        MessageReadEvent event = MessageReadEvent.of(
                roomId.toStringValue(),
                userId.getValue(),
                0
        );
        eventPublisher.publish(event);
    }

    private void validateAccess(GetMessagesQuery query) {
        ChatRoom chatRoom = chatRoomRepository.findById(query.roomId())
                .orElseThrow(() -> ChatException.of(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.isParticipant(query.userId())) {
            throw ChatException.of(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }

    private List<Message> fetchMessages(GetMessagesQuery query) {
        if (query.hasCursor()) {
            return messageRepository.findByRoomIdBeforeCursor(
                    query.roomId(),
                    query.cursorId(),
                    query.limit()
            );
        }

        return messageRepository.findByRoomIdOrderByCreatedAtDesc(
                query.roomId(),
                query.limit(),
                0
        );
    }

}
