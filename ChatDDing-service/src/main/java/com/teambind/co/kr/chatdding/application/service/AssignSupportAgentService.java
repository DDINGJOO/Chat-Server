package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.AssignSupportAgentCommand;
import com.teambind.co.kr.chatdding.application.port.in.AssignSupportAgentResult;
import com.teambind.co.kr.chatdding.application.port.in.AssignSupportAgentUseCase;
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher;
import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;
import com.teambind.co.kr.chatdding.domain.event.SupportAgentAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 상담원 배정 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AssignSupportAgentService implements AssignSupportAgentUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final EventPublisher eventPublisher;

    @Override
    public AssignSupportAgentResult execute(AssignSupportAgentCommand command) {
        ChatRoom chatRoom = findAndValidateChatRoom(command);

        LocalDateTime assignedAt = LocalDateTime.now();
        chatRoom.assignAgent(command.agentId());
        chatRoomRepository.save(chatRoom);

        publishSupportAgentAssignedEvent(chatRoom, command);

        return AssignSupportAgentResult.from(chatRoom, command.agentId(), assignedAt);
    }

    private ChatRoom findAndValidateChatRoom(AssignSupportAgentCommand command) {
        ChatRoom chatRoom = chatRoomRepository.findById(command.roomId())
                .orElseThrow(() -> ChatException.of(ErrorCode.ROOM_NOT_FOUND));

        if (chatRoom.getType() != ChatRoomType.SUPPORT) {
            throw ChatException.of(ErrorCode.NOT_SUPPORT_ROOM);
        }

        if (!chatRoom.isActive()) {
            throw ChatException.of(ErrorCode.ROOM_ALREADY_CLOSED);
        }

        return chatRoom;
    }

    private void publishSupportAgentAssignedEvent(ChatRoom chatRoom, AssignSupportAgentCommand command) {
        SupportAgentAssignedEvent event = SupportAgentAssignedEvent.from(chatRoom, command.agentId());
        eventPublisher.publish(event);
    }
}
