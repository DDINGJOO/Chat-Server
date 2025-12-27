package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsResult;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsUseCase;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.message.Message;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 채팅방 목록 조회 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetChatRoomsService implements GetChatRoomsUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    @Override
    public GetChatRoomsResult execute(GetChatRoomsQuery query) {
        List<ChatRoom> chatRooms = chatRoomRepository
                .findActiveByParticipantUserIdOrderByLastMessageAtDesc(query.userId());

        List<GetChatRoomsResult.ChatRoomItem> items = chatRooms.stream()
                .map(chatRoom -> buildChatRoomItem(chatRoom, query))
                .toList();

        return new GetChatRoomsResult(items);
    }

    private GetChatRoomsResult.ChatRoomItem buildChatRoomItem(ChatRoom chatRoom, GetChatRoomsQuery query) {
        Message lastMessage = messageRepository.findLatestByRoomId(chatRoom.getId())
                .orElse(null);

        long unreadCount = messageRepository.countUnreadByRoomIdAndUserId(
                chatRoom.getId(),
                query.userId()
        );

        return GetChatRoomsResult.ChatRoomItem.from(chatRoom, lastMessage, unreadCount);
    }
}
