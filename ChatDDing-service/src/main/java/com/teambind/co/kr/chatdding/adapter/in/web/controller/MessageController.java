package com.teambind.co.kr.chatdding.adapter.in.web.controller;

import com.teambind.co.kr.chatdding.adapter.in.web.dto.ApiResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.request.MarkAsReadRequest;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.request.SendMessageRequest;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.DeleteMessageResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.GetMessagesResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.MarkAsReadResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.SendMessageResponse;
import com.teambind.co.kr.chatdding.application.port.in.DeleteMessageCommand;
import com.teambind.co.kr.chatdding.application.port.in.DeleteMessageResult;
import com.teambind.co.kr.chatdding.application.port.in.DeleteMessageUseCase;
import com.teambind.co.kr.chatdding.application.port.in.GetMessagesQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetMessagesResult;
import com.teambind.co.kr.chatdding.application.port.in.GetMessagesUseCase;
import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadCommand;
import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadResult;
import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadUseCase;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageResult;
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메시지 API Controller (Web Adapter)
 */
@RestController
@RequestMapping("/api/v1/rooms/{roomId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final SendMessageUseCase sendMessageUseCase;
    private final GetMessagesUseCase getMessagesUseCase;
    private final MarkAsReadUseCase markAsReadUseCase;
    private final DeleteMessageUseCase deleteMessageUseCase;

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

    /**
     * 메시지 목록 조회 (커서 기반 페이징)
     *
     * GET /api/v1/rooms/{roomId}/messages
     */
    @GetMapping
    public ResponseEntity<ApiResponse<GetMessagesResponse>> getMessages(
            @PathVariable String roomId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") Integer limit
    ) {
        GetMessagesResult result = getMessagesUseCase.execute(
                GetMessagesQuery.of(roomId, userId, cursor, limit)
        );

        return ResponseEntity.ok(ApiResponse.success(GetMessagesResponse.from(result)));
    }

    /**
     * 읽음 처리
     *
     * POST /api/v1/rooms/{roomId}/messages/read
     */
    @PostMapping("/read")
    public ResponseEntity<ApiResponse<MarkAsReadResponse>> markAsRead(
            @PathVariable String roomId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody(required = false) MarkAsReadRequest request
    ) {
        MarkAsReadResult result = markAsReadUseCase.execute(
                request != null
                        ? request.toCommand(roomId, userId)
                        : MarkAsReadCommand.of(roomId, userId)
        );

        return ResponseEntity.ok(ApiResponse.success(MarkAsReadResponse.from(result)));
    }

    /**
     * 메시지 삭제
     *
     * DELETE /api/v1/rooms/{roomId}/messages/{messageId}
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<DeleteMessageResponse>> deleteMessage(
            @PathVariable String roomId,
            @PathVariable String messageId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        DeleteMessageResult result = deleteMessageUseCase.execute(
                DeleteMessageCommand.of(roomId, messageId, userId)
        );

        return ResponseEntity.ok(ApiResponse.success(DeleteMessageResponse.from(result)));
    }
}
