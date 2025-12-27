package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesResult;
import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesResult.InquiryItem;
import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesUseCase;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 호스트 문의 목록 조회 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetHostInquiriesService implements GetHostInquiriesUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    @Override
    public GetHostInquiriesResult execute(GetHostInquiriesQuery query) {
        List<ChatRoom> inquiries = chatRoomRepository.findPlaceInquiriesByHostId(
                query.hostId(),
                query.placeId()
        );

        if (inquiries.isEmpty()) {
            return GetHostInquiriesResult.empty();
        }

        List<InquiryItem> items = inquiries.stream()
                .map(chatRoom -> toInquiryItem(chatRoom, query.hostId()))
                .limit(query.limit())
                .toList();

        boolean hasMore = inquiries.size() > query.limit();
        String nextCursor = hasMore && !items.isEmpty()
                ? items.get(items.size() - 1).roomId()
                : null;

        return GetHostInquiriesResult.of(items, nextCursor, hasMore);
    }

    private InquiryItem toInquiryItem(ChatRoom chatRoom, UserId hostId) {
        Long guestId = chatRoom.getParticipantIds().stream()
                .filter(userId -> !userId.equals(hostId))
                .findFirst()
                .map(UserId::getValue)
                .orElse(null);

        int unreadCount = (int) messageRepository.countUnreadByRoomIdAndUserId(
                chatRoom.getId(),
                hostId
        );

        return InquiryItem.from(chatRoom, guestId, unreadCount);
    }
}
