package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.GetMessagesQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetMessagesResult;
import com.teambind.co.kr.chatdding.application.port.in.GetMessagesUseCase;
import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.message.Message;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 메시지 조회 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMessagesService implements GetMessagesUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    @Override
    public GetMessagesResult execute(GetMessagesQuery query) {
        validateAccess(query);

        List<Message> messages = fetchMessages(query);
        List<Message> visibleMessages = filterVisibleMessages(messages, query.userId());

        return GetMessagesResult.of(visibleMessages, query.limit());
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

    private List<Message> filterVisibleMessages(List<Message> messages, UserId userId) {
        return messages.stream()
                .filter(message -> message.isVisibleTo(userId))
                .toList();
    }
}
