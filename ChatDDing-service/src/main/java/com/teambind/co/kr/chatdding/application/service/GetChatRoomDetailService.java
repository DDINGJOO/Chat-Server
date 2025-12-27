package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailResult;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailUseCase;
import com.teambind.co.kr.chatdding.common.exception.ChatException;
import com.teambind.co.kr.chatdding.common.exception.ErrorCode;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅방 상세 조회 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetChatRoomDetailService implements GetChatRoomDetailUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    @Override
    public GetChatRoomDetailResult execute(GetChatRoomDetailQuery query) {
        ChatRoom chatRoom = chatRoomRepository.findById(query.roomId())
                .orElseThrow(() -> ChatException.of(ErrorCode.CHAT_ROOM_NOT_FOUND));

        validateAccess(chatRoom, query);

        long unreadCount = messageRepository.countUnreadByRoomIdAndUserId(
                query.roomId(),
                query.userId()
        );

        return GetChatRoomDetailResult.from(chatRoom, unreadCount);
    }

    private void validateAccess(ChatRoom chatRoom, GetChatRoomDetailQuery query) {
        if (!chatRoom.isParticipant(query.userId())) {
            throw ChatException.of(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
}
