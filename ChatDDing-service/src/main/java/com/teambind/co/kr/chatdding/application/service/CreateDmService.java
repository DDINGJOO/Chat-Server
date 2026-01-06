package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.CreateDmCommand;
import com.teambind.co.kr.chatdding.application.port.in.CreateDmResult;
import com.teambind.co.kr.chatdding.application.port.in.CreateDmUseCase;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageCommand;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase;
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher;
import com.teambind.co.kr.chatdding.common.util.generator.PrimaryKeyGenerator;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.event.DmCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * DM 채팅방 생성 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateDmService implements CreateDmUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final PrimaryKeyGenerator primaryKeyGenerator;
    private final EventPublisher eventPublisher;
    private final SendMessageUseCase sendMessageUseCase;

    @Override
    public CreateDmResult execute(CreateDmCommand command) {
        // 기존 DM 채팅방이 있는지 확인
        List<Long> sortedParticipantIds = getSortedParticipantIds(command);
        Optional<ChatRoom> existingDm = chatRoomRepository.findDmByParticipantIds(sortedParticipantIds);

        if (existingDm.isPresent()) {
            // 기존 채팅방 반환
            return CreateDmResult.from(existingDm.get(), false);
        }

        // 새 DM 채팅방 생성
        ChatRoom chatRoom = createAndSaveChatRoom(command);
        sendInitialMessageIfPresent(command, chatRoom);
        publishDmCreatedEvent(chatRoom, command.senderId().getValue());

        return CreateDmResult.from(chatRoom, true);
    }

    private List<Long> getSortedParticipantIds(CreateDmCommand command) {
        return Stream.of(command.senderId().getValue(), command.recipientId().getValue())
                .sorted()
                .toList();
    }

    private ChatRoom createAndSaveChatRoom(CreateDmCommand command) {
        RoomId roomId = RoomId.of(primaryKeyGenerator.generateLongKey());

        ChatRoom chatRoom = ChatRoom.createDm(
                roomId,
                command.senderId(),
                command.recipientId()
        );

        return chatRoomRepository.save(chatRoom);
    }

    private void sendInitialMessageIfPresent(CreateDmCommand command, ChatRoom chatRoom) {
        if (command.hasInitialMessage()) {
            SendMessageCommand messageCommand = new SendMessageCommand(
                    chatRoom.getId(),
                    command.senderId(),
                    command.initialMessage()
            );
            sendMessageUseCase.execute(messageCommand);
        }
    }

    private void publishDmCreatedEvent(ChatRoom chatRoom, Long senderId) {
        DmCreatedEvent event = DmCreatedEvent.from(chatRoom, senderId);
        eventPublisher.publish(event);
    }
}
