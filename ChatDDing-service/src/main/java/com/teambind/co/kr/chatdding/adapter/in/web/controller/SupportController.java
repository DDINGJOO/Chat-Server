package com.teambind.co.kr.chatdding.adapter.in.web.controller;

import com.teambind.co.kr.chatdding.adapter.in.web.dto.ApiResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.request.CreateSupportRequest;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.AssignAgentResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.CloseSupportResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.CreateSupportResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.GetSupportQueueResponse;
import com.teambind.co.kr.chatdding.application.port.in.AssignSupportAgentCommand;
import com.teambind.co.kr.chatdding.application.port.in.AssignSupportAgentResult;
import com.teambind.co.kr.chatdding.application.port.in.AssignSupportAgentUseCase;
import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatCommand;
import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatResult;
import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatUseCase;
import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestResult;
import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestUseCase;
import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueResult;
import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 고객 상담 API Controller (Web Adapter)
 */
@RestController
@RequestMapping("/api/v1/chat/support")
@RequiredArgsConstructor
public class SupportController {

    private final CreateSupportRequestUseCase createSupportRequestUseCase;
    private final GetSupportQueueUseCase getSupportQueueUseCase;
    private final AssignSupportAgentUseCase assignSupportAgentUseCase;
    private final CloseSupportChatUseCase closeSupportChatUseCase;

    /**
     * 상담 요청 생성
     *
     * POST /api/v1/chat/support
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSupportResponse>> createSupportRequest(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody(required = false) CreateSupportRequest request
    ) {
        CreateSupportRequestResult result = createSupportRequestUseCase.execute(
                request != null ? request.toCommand(userId) : createDefaultCommand(userId)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(CreateSupportResponse.from(result)));
    }

    /**
     * 상담 대기열 조회
     *
     * GET /api/v1/chat/support/queue
     */
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<GetSupportQueueResponse>> getSupportQueue(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        GetSupportQueueResult result = getSupportQueueUseCase.execute(
                GetSupportQueueQuery.of(cursor, limit)
        );

        return ResponseEntity.ok(ApiResponse.success(GetSupportQueueResponse.from(result)));
    }

    /**
     * 상담원 배정
     *
     * POST /api/v1/chat/support/{roomId}/assign
     */
    @PostMapping("/{roomId}/assign")
    public ResponseEntity<ApiResponse<AssignAgentResponse>> assignAgent(
            @RequestHeader("X-Agent-Id") Long agentId,
            @PathVariable String roomId
    ) {
        AssignSupportAgentResult result = assignSupportAgentUseCase.execute(
                AssignSupportAgentCommand.of(roomId, agentId)
        );

        return ResponseEntity.ok(ApiResponse.success(AssignAgentResponse.from(result)));
    }

    /**
     * 상담 종료
     *
     * POST /api/v1/chat/support/{roomId}/close
     */
    @PostMapping("/{roomId}/close")
    public ResponseEntity<ApiResponse<CloseSupportResponse>> closeSupportChat(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String roomId
    ) {
        CloseSupportChatResult result = closeSupportChatUseCase.execute(
                CloseSupportChatCommand.of(roomId, userId)
        );

        return ResponseEntity.ok(ApiResponse.success(CloseSupportResponse.from(result)));
    }

    private com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestCommand createDefaultCommand(Long userId) {
        return com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestCommand.of(userId);
    }
}
