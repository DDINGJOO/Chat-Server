package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.SendMessageCommand;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageResult;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase;
import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.common.util.generator.PrimaryKeyGenerator;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.message.Message;
import com.teambind.co.kr.chatdding.domain.message.MessageId;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메시지 전송 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SendMessageService implements SendMessageUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final PrimaryKeyGenerator primaryKeyGenerator;

    @Override
    public SendMessageResult execute(SendMessageCommand command) {
        ChatRoom chatRoom = findChatRoom(command);
        validateParticipant(chatRoom, command);

        Message message = createAndSaveMessage(command);
        updateChatRoomLastMessageAt(chatRoom, message);

        return SendMessageResult.from(message);
    }

    private ChatRoom findChatRoom(SendMessageCommand command) {
        return chatRoomRepository.findById(command.roomId())
                .orElseThrow(() -> ChatException.of(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private void validateParticipant(ChatRoom chatRoom, SendMessageCommand command) {
        if (!chatRoom.isParticipant(command.senderId())) {
            throw ChatException.of(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        if (!chatRoom.isActive()) {
            throw ChatException.of(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }

    private Message createAndSaveMessage(SendMessageCommand command) {
        MessageId messageId = MessageId.of(primaryKeyGenerator.generateLongKey());

        Message message = Message.create(
                messageId,
                command.roomId(),
                command.senderId(),
                command.content()
        );

        return messageRepository.save(message);
    }

    private void updateChatRoomLastMessageAt(ChatRoom chatRoom, Message message) {
        chatRoom.updateLastMessageAt(message.getCreatedAt());
        chatRoomRepository.save(chatRoom);
    }
}
