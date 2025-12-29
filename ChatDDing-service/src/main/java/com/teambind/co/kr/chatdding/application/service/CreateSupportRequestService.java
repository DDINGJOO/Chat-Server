package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestCommand;
import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestResult;
import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestUseCase;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageCommand;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase;
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher;
import com.teambind.co.kr.chatdding.common.util.generator.PrimaryKeyGenerator;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.event.SupportRequestCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상담 요청 생성 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateSupportRequestService implements CreateSupportRequestUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final PrimaryKeyGenerator primaryKeyGenerator;
    private final EventPublisher eventPublisher;
    private final SendMessageUseCase sendMessageUseCase;

    @Override
    public CreateSupportRequestResult execute(CreateSupportRequestCommand command) {
        ChatRoom chatRoom = createAndSaveChatRoom(command);
        sendInitialMessageIfPresent(command, chatRoom);
        publishSupportRequestCreatedEvent(chatRoom, command.category());

        return CreateSupportRequestResult.from(chatRoom, command.category());
    }

    private ChatRoom createAndSaveChatRoom(CreateSupportRequestCommand command) {
        RoomId roomId = RoomId.of(primaryKeyGenerator.generateLongKey());
        ChatRoom chatRoom = ChatRoom.createSupport(roomId, command.userId());
        return chatRoomRepository.save(chatRoom);
    }

    private void sendInitialMessageIfPresent(CreateSupportRequestCommand command, ChatRoom chatRoom) {
        if (command.hasInitialMessage()) {
            SendMessageCommand messageCommand = new SendMessageCommand(
                    chatRoom.getId(),
                    command.userId(),
                    command.initialMessage()
            );
            sendMessageUseCase.execute(messageCommand);
        }
    }

    private void publishSupportRequestCreatedEvent(ChatRoom chatRoom, String category) {
        SupportRequestCreatedEvent event = SupportRequestCreatedEvent.from(chatRoom, category);
        eventPublisher.publish(event);
    }
}
