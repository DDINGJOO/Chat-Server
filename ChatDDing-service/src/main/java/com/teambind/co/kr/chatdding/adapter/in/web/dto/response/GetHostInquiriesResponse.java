package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 호스트 문의 목록 Response DTO
 */
public record GetHostInquiriesResponse(
        List<InquiryItemDto> inquiries,
        String nextCursor,
        boolean hasMore
) {

    public record InquiryItemDto(
            String roomId,
            Long guestId,
            String guestNickname,
            ContextDto context,
            String lastMessage,
            int unreadCount,
            LocalDateTime lastMessageAt
    ) {
        public static InquiryItemDto from(GetHostInquiriesResult.InquiryItem item) {
            return new InquiryItemDto(
                    item.roomId(),
                    item.guestId(),
                    item.guestNickname(),
                    item.context() != null ? ContextDto.from(item.context()) : null,
                    item.lastMessage(),
                    item.unreadCount(),
                    item.lastMessageAt()
            );
        }
    }

    public record ContextDto(
            String contextType,
            Long contextId,
            String contextName
    ) {
        public static ContextDto from(GetHostInquiriesResult.ContextDto context) {
            return new ContextDto(
                    context.contextType(),
                    context.contextId(),
                    context.contextName()
            );
        }
    }

    public static GetHostInquiriesResponse from(GetHostInquiriesResult result) {
        List<InquiryItemDto> items = result.inquiries().stream()
                .map(InquiryItemDto::from)
                .toList();

        return new GetHostInquiriesResponse(items, result.nextCursor(), result.hasMore());
    }
}
