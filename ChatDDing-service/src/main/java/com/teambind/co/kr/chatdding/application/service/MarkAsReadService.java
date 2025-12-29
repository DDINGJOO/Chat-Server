package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadCommand;
import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadResult;
import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadUseCase;
import com.teambind.co.kr.chatdding.application.port.out.UnreadCountCachePort;
import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.Participant;
import com.teambind.co.kr.chatdding.domain.message.Message;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 읽음 처리 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MarkAsReadService implements MarkAsReadUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UnreadCountCachePort unreadCountCachePort;

    @Override
    public MarkAsReadResult execute(MarkAsReadCommand command) {
        ChatRoom chatRoom = validateAndGetChatRoom(command);
        LocalDateTime readAt = LocalDateTime.now();

        List<Message> unreadMessages = getUnreadMessages(command);
        int readCount = markMessagesAsRead(unreadMessages, command, readAt);

        updateParticipantLastReadAt(chatRoom, command, readAt);
        resetUnreadCountCache(command);

        return MarkAsReadResult.of(
                command.roomId().toStringValue(),
                command.userId().getValue(),
                readAt,
                readCount
        );
    }

    private ChatRoom validateAndGetChatRoom(MarkAsReadCommand command) {
        ChatRoom chatRoom = chatRoomRepository.findById(command.roomId())
                .orElseThrow(() -> ChatException.of(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.isParticipant(command.userId())) {
            throw ChatException.of(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        return chatRoom;
    }

    private List<Message> getUnreadMessages(MarkAsReadCommand command) {
        if (command.lastMessageId() != null) {
            Message lastMessage = messageRepository.findById(command.lastMessageId())
                    .orElseThrow(() -> ChatException.of(ErrorCode.MESSAGE_NOT_FOUND));

            return messageRepository.findByRoomIdAndCreatedAtAfter(
                    command.roomId(),
                    lastMessage.getCreatedAt().minusSeconds(1)
            );
        }

        return messageRepository.findByRoomIdOrderByCreatedAtDesc(
                command.roomId(),
                100,
                0
        );
    }

    private int markMessagesAsRead(List<Message> messages, MarkAsReadCommand command, LocalDateTime readAt) {
        int count = 0;
        for (Message message : messages) {
            if (!message.isReadBy(command.userId())) {
                message.markAsReadAt(command.userId(), readAt);
                messageRepository.save(message);
                count++;
            }
        }
        return count;
    }

    private void updateParticipantLastReadAt(ChatRoom chatRoom, MarkAsReadCommand command, LocalDateTime readAt) {
        chatRoom.findParticipant(command.userId())
                .ifPresent(participant -> {
                    participant.updateLastReadAt(readAt);
                    chatRoomRepository.save(chatRoom);
                });
    }

    private void resetUnreadCountCache(MarkAsReadCommand command) {
        unreadCountCachePort.resetUnreadCount(command.roomId(), command.userId());
    }
}
