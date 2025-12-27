package com.teambind.co.kr.chatdding.application.port.in;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomContext;
import com.teambind.co.kr.chatdding.domain.common.UserId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 호스트 문의 목록 조회 Result DTO
 */
public record GetHostInquiriesResult(
        List<InquiryItem> inquiries,
        String nextCursor,
        boolean hasMore
) {

    public record InquiryItem(
            String roomId,
            Long guestId,
            String guestNickname,
            ContextDto context,
            String lastMessage,
            int unreadCount,
            LocalDateTime lastMessageAt
    ) {

        public static InquiryItem from(ChatRoom chatRoom, Long guestId, int unreadCount) {
            return new InquiryItem(
                    chatRoom.getId().toStringValue(),
                    guestId,
                    null,  // nickname은 별도 조회 필요
                    ContextDto.from(chatRoom.getContext()),
                    null,  // lastMessage는 별도 조회 필요
                    unreadCount,
                    chatRoom.getLastMessageAt()
            );
        }
    }

    public record ContextDto(
            String contextType,
            Long contextId,
            String contextName
    ) {
        public static ContextDto from(ChatRoomContext context) {
            if (context == null) {
                return null;
            }
            return new ContextDto(
                    context.contextType().name(),
                    context.contextId(),
                    context.contextName()
            );
        }
    }

    public static GetHostInquiriesResult of(List<InquiryItem> inquiries, String nextCursor, boolean hasMore) {
        return new GetHostInquiriesResult(inquiries, nextCursor, hasMore);
    }

    public static GetHostInquiriesResult empty() {
        return new GetHostInquiriesResult(List.of(), null, false);
    }
}
