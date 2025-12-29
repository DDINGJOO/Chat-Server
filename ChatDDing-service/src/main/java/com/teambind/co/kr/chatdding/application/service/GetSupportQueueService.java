package com.teambind.co.kr.chatdding.application.service;

import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueResult;
import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueResult.SupportQueueItem;
import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueUseCase;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 상담 대기열 조회 UseCase 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSupportQueueService implements GetSupportQueueUseCase {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public GetSupportQueueResult execute(GetSupportQueueQuery query) {
        List<ChatRoom> pendingRooms = chatRoomRepository.findPendingSupportRooms(
                query.cursor(),
                query.limit() + 1
        );

        boolean hasMore = pendingRooms.size() > query.limit();
        if (hasMore) {
            pendingRooms = pendingRooms.subList(0, query.limit());
        }

        List<SupportQueueItem> items = pendingRooms.stream()
                .map(room -> SupportQueueItem.from(room, null))
                .toList();

        String nextCursor = hasMore && !items.isEmpty()
                ? items.get(items.size() - 1).roomId()
                : null;

        long totalCount = chatRoomRepository.countPendingSupportRooms();

        return GetSupportQueueResult.of(items, nextCursor, totalCount);
    }
}
