package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryCommand;
import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryResult;
import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryUseCase;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageCommand;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase;
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher;
import com.teambind.co.kr.chatdding.common.util.generator.PrimaryKeyGenerator;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomContext;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.event.InquiryCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공간 문의 생성 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreatePlaceInquiryService implements CreatePlaceInquiryUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final PrimaryKeyGenerator primaryKeyGenerator;
    private final EventPublisher eventPublisher;
    private final SendMessageUseCase sendMessageUseCase;

    @Override
    public CreatePlaceInquiryResult execute(CreatePlaceInquiryCommand command) {
        return chatRoomRepository.findPlaceInquiryByPlaceIdAndGuestId(command.placeId(), command.guestId())
                .map(existingRoom -> handleExistingInquiry(existingRoom, command))
                .orElseGet(() -> createNewInquiry(command));
    }

    private CreatePlaceInquiryResult handleExistingInquiry(ChatRoom existingRoom, CreatePlaceInquiryCommand command) {
        sendInitialMessageIfPresent(command, existingRoom);
        return CreatePlaceInquiryResult.from(existingRoom);
    }

    private CreatePlaceInquiryResult createNewInquiry(CreatePlaceInquiryCommand command) {
        ChatRoom chatRoom = createAndSaveChatRoom(command);
        sendInitialMessageIfPresent(command, chatRoom);
        publishInquiryCreatedEvent(chatRoom);
        return CreatePlaceInquiryResult.from(chatRoom);
    }

    private ChatRoom createAndSaveChatRoom(CreatePlaceInquiryCommand command) {
        RoomId roomId = RoomId.of(primaryKeyGenerator.generateLongKey());
        ChatRoomContext context = ChatRoomContext.forPlace(command.placeId(), command.placeName());

        ChatRoom chatRoom = ChatRoom.createPlaceInquiry(
                roomId,
                command.guestId(),
                command.hostId(),
                context
        );

        return chatRoomRepository.save(chatRoom);
    }

    private void sendInitialMessageIfPresent(CreatePlaceInquiryCommand command, ChatRoom chatRoom) {
        if (command.hasInitialMessage()) {
            SendMessageCommand messageCommand = new SendMessageCommand(
                    chatRoom.getId(),
                    command.guestId(),
                    command.initialMessage()
            );
            sendMessageUseCase.execute(messageCommand);
        }
    }

    private void publishInquiryCreatedEvent(ChatRoom chatRoom) {
        InquiryCreatedEvent event = InquiryCreatedEvent.from(chatRoom);
        eventPublisher.publish(event);
    }
}
