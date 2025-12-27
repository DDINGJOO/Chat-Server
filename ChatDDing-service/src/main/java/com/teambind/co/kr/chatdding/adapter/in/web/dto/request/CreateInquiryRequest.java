package com.teambind.co.kr.chatdding.adapter.in.web.dto.request;

import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 공간 문의 생성 Request DTO
 */
public record CreateInquiryRequest(
        @NotNull(message = "placeId is required")
        Long placeId,

        @NotBlank(message = "placeName is required")
        String placeName,

        @NotNull(message = "hostId is required")
        Long hostId,

        @Size(max = 5000, message = "initialMessage must be less than 5000 characters")
        String initialMessage
) {

    public CreatePlaceInquiryCommand toCommand(Long guestId) {
        return CreatePlaceInquiryCommand.of(guestId, hostId, placeId, placeName, initialMessage);
    }
}
