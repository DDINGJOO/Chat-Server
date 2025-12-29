package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsResult;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsUseCase;
import com.teambind.co.kr.chatdding.application.port.out.UnreadCountCachePort;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.message.Message;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 채팅방 목록 조회 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetChatRoomsService implements GetChatRoomsUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UnreadCountCachePort unreadCountCachePort;

    @Override
    public GetChatRoomsResult execute(GetChatRoomsQuery query) {
        List<ChatRoom> chatRooms = chatRoomRepository
                .findActiveByParticipantUserIdOrderByLastMessageAtDesc(query.userId());

        List<RoomId> roomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .toList();

        Map<RoomId, Integer> cachedUnreadCounts = unreadCountCachePort.getUnreadCounts(roomIds, query.userId());

        List<GetChatRoomsResult.ChatRoomItem> items = chatRooms.stream()
                .map(chatRoom -> buildChatRoomItem(chatRoom, query, cachedUnreadCounts))
                .toList();

        return new GetChatRoomsResult(items);
    }

    private GetChatRoomsResult.ChatRoomItem buildChatRoomItem(
            ChatRoom chatRoom,
            GetChatRoomsQuery query,
            Map<RoomId, Integer> cachedUnreadCounts
    ) {
        Message lastMessage = messageRepository.findLatestByRoomId(chatRoom.getId())
                .orElse(null);

        long unreadCount = getUnreadCountWithCacheAside(chatRoom.getId(), query.userId(), cachedUnreadCounts);

        return GetChatRoomsResult.ChatRoomItem.from(chatRoom, lastMessage, unreadCount);
    }

    private long getUnreadCountWithCacheAside(RoomId roomId, UserId userId, Map<RoomId, Integer> cachedCounts) {
        if (cachedCounts.containsKey(roomId)) {
            return cachedCounts.get(roomId);
        }

        long count = messageRepository.countUnreadByRoomIdAndUserId(roomId, userId);
        unreadCountCachePort.setUnreadCount(roomId, userId, (int) count);
        return count;
    }
}
