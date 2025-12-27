package com.teambind.co.kr.chatdding.adapter.in.web.controller;

import com.teambind.co.kr.chatdding.adapter.in.web.dto.ApiResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.SendMessageRequest;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.SendMessageResponse;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageResult;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메시지 API Controller (Web Adapter)
 */
@RestController
@RequestMapping("/api/v1/rooms/{roomId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final SendMessageUseCase sendMessageUseCase;

    /**
     * 메시지 전송
     *
     * POST /api/v1/rooms/{roomId}/messages
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SendMessageResponse>> sendMessage(
            @PathVariable String roomId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        SendMessageResult result = sendMessageUseCase.execute(
                request.toCommand(roomId, userId)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SendMessageResponse.from(result)));
    }
}
