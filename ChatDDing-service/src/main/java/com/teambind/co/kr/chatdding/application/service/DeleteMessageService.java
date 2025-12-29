package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.DeleteMessageCommand;
import com.teambind.co.kr.chatdding.application.port.in.DeleteMessageResult;
import com.teambind.co.kr.chatdding.application.port.in.DeleteMessageUseCase;
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher;
import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.event.MessageDeletedEvent;
import com.teambind.co.kr.chatdding.domain.message.Message;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메시지 삭제 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DeleteMessageService implements DeleteMessageUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final EventPublisher eventPublisher;

    @Override
    public DeleteMessageResult execute(DeleteMessageCommand command) {
        ChatRoom chatRoom = validateAndGetChatRoom(command);
        Message message = validateAndGetMessage(command);

        // Soft Delete
        message.deleteFor(command.userId());

        // Hard Delete 조건 체크
        int participantCount = chatRoom.getParticipants().size();
        boolean shouldHardDelete = message.shouldHardDelete(participantCount);

        if (shouldHardDelete) {
            messageRepository.deleteById(command.messageId());
        } else {
            messageRepository.save(message);
        }

        // 이벤트 발행
        publishEvent(command, shouldHardDelete);

        return DeleteMessageResult.of(
                command.messageId().toStringValue(),
                shouldHardDelete
        );
    }

    private ChatRoom validateAndGetChatRoom(DeleteMessageCommand command) {
        ChatRoom chatRoom = chatRoomRepository.findById(command.roomId())
                .orElseThrow(() -> ChatException.of(ErrorCode.ROOM_NOT_FOUND));

        if (!chatRoom.isParticipant(command.userId())) {
            throw ChatException.of(ErrorCode.NOT_PARTICIPANT);
        }

        return chatRoom;
    }

    private Message validateAndGetMessage(DeleteMessageCommand command) {
        Message message = messageRepository.findById(command.messageId())
                .orElseThrow(() -> ChatException.of(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getRoomId().equals(command.roomId())) {
            throw ChatException.of(ErrorCode.MESSAGE_NOT_FOUND);
        }

        return message;
    }

    private void publishEvent(DeleteMessageCommand command, boolean hardDeleted) {
        MessageDeletedEvent event = MessageDeletedEvent.of(
                command.messageId().toStringValue(),
                command.roomId().toStringValue(),
                command.userId().getValue(),
                hardDeleted
        );
        eventPublisher.publish(event);
    }
}
