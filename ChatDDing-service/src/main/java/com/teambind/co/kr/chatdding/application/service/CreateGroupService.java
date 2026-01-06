package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.CreateGroupCommand;
import com.teambind.co.kr.chatdding.application.port.in.CreateGroupResult;
import com.teambind.co.kr.chatdding.application.port.in.CreateGroupUseCase;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageCommand;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase;
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher;
import com.teambind.co.kr.chatdding.common.util.generator.PrimaryKeyGenerator;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.event.GroupCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 그룹 채팅방 생성 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateGroupService implements CreateGroupUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final PrimaryKeyGenerator primaryKeyGenerator;
    private final EventPublisher eventPublisher;
    private final SendMessageUseCase sendMessageUseCase;

    @Override
    public CreateGroupResult execute(CreateGroupCommand command) {
        ChatRoom chatRoom = createAndSaveChatRoom(command);
        sendInitialMessageIfPresent(command, chatRoom);
        publishGroupCreatedEvent(chatRoom);

        return CreateGroupResult.from(chatRoom);
    }

    private ChatRoom createAndSaveChatRoom(CreateGroupCommand command) {
        RoomId roomId = RoomId.of(primaryKeyGenerator.generateLongKey());

        ChatRoom chatRoom = ChatRoom.createGroup(
                roomId,
                command.ownerId(),
                command.memberIds(),
                command.name()
        );

        return chatRoomRepository.save(chatRoom);
    }

    private void sendInitialMessageIfPresent(CreateGroupCommand command, ChatRoom chatRoom) {
        if (command.hasInitialMessage()) {
            SendMessageCommand messageCommand = new SendMessageCommand(
                    chatRoom.getId(),
                    command.ownerId(),
                    command.initialMessage()
            );
            sendMessageUseCase.execute(messageCommand);
        }
    }

    private void publishGroupCreatedEvent(ChatRoom chatRoom) {
        GroupCreatedEvent event = GroupCreatedEvent.from(chatRoom);
        eventPublisher.publish(event);
    }
}
