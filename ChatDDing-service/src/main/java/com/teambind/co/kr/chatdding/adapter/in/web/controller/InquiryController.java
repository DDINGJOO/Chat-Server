package com.teambind.co.kr.chatdding.adapter.in.web.controller;

import com.teambind.co.kr.chatdding.adapter.in.web.dto.ApiResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.request.CreateInquiryRequest;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.CreateInquiryResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.GetHostInquiriesResponse;
import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryResult;
import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryUseCase;
import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesResult;
import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공간 문의 API Controller (Web Adapter)
 */
@RestController
@RequestMapping("/api/v1/chat/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final CreatePlaceInquiryUseCase createPlaceInquiryUseCase;
    private final GetHostInquiriesUseCase getHostInquiriesUseCase;

    /**
     * 공간 문의 생성
     *
     * POST /api/v1/chat/inquiry
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateInquiryResponse>> createInquiry(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateInquiryRequest request
    ) {
        CreatePlaceInquiryResult result = createPlaceInquiryUseCase.execute(
                request.toCommand(userId)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(CreateInquiryResponse.from(result)));
    }

    /**
     * 호스트 문의 목록 조회
     *
     * GET /api/v1/chat/inquiry/host
     */
    @GetMapping("/host")
    public ResponseEntity<ApiResponse<GetHostInquiriesResponse>> getHostInquiries(
            @RequestHeader("X-User-Id") Long hostId,
            @RequestParam(required = false) Long placeId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        GetHostInquiriesResult result = getHostInquiriesUseCase.execute(
                GetHostInquiriesQuery.of(hostId, placeId, cursor, limit)
        );

        return ResponseEntity.ok(ApiResponse.success(GetHostInquiriesResponse.from(result)));
    }
}
