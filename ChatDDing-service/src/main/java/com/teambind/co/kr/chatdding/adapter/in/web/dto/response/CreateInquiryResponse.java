package com.teambind.co.kr.chatdding.adapter.in.web.dto.response;

import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryResult;

import java.time.LocalDateTime;

/**
 * 공간 문의 생성 Response DTO
 */
public record CreateInquiryResponse(
        String roomId,
        String type,
        ContextDto context,
        LocalDateTime createdAt
) {

    public record ContextDto(
            String contextType,
            Long contextId,
            String contextName
    ) {
        public static ContextDto from(CreatePlaceInquiryResult.ContextDto context) {
            return new ContextDto(
                    context.contextType(),
                    context.contextId(),
                    context.contextName()
            );
        }
    }

    public static CreateInquiryResponse from(CreatePlaceInquiryResult result) {
        return new CreateInquiryResponse(
                result.roomId(),
                result.type(),
                ContextDto.from(result.context()),
                result.createdAt()
        );
    }
}
