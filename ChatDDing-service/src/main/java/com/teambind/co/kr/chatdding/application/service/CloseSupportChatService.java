package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatCommand;
import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatResult;
import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatUseCase;
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher;
import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;
import com.teambind.co.kr.chatdding.domain.event.SupportChatClosedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 상담 종료 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CloseSupportChatService implements CloseSupportChatUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final EventPublisher eventPublisher;

    @Override
    public CloseSupportChatResult execute(CloseSupportChatCommand command) {
        ChatRoom chatRoom = findAndValidateChatRoom(command);

        LocalDateTime closedAt = LocalDateTime.now();
        chatRoom.close();
        chatRoomRepository.save(chatRoom);

        publishSupportChatClosedEvent(chatRoom);

        return CloseSupportChatResult.from(chatRoom, closedAt);
    }

    private ChatRoom findAndValidateChatRoom(CloseSupportChatCommand command) {
        ChatRoom chatRoom = chatRoomRepository.findById(command.roomId())
                .orElseThrow(() -> ChatException.of(ErrorCode.ROOM_NOT_FOUND));

        if (chatRoom.getType() != ChatRoomType.SUPPORT) {
            throw ChatException.of(ErrorCode.NOT_SUPPORT_ROOM);
        }

        if (!chatRoom.isActive()) {
            throw ChatException.of(ErrorCode.ROOM_ALREADY_CLOSED);
        }

        if (!chatRoom.isParticipant(command.userId())) {
            throw ChatException.of(ErrorCode.NOT_PARTICIPANT);
        }

        return chatRoom;
    }

    private void publishSupportChatClosedEvent(ChatRoom chatRoom) {
        SupportChatClosedEvent event = SupportChatClosedEvent.from(chatRoom);
        eventPublisher.publish(event);
    }
}
