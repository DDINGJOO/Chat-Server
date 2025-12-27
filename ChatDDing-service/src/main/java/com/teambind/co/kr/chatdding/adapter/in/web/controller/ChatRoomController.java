package com.teambind.co.kr.chatdding.adapter.in.web.controller;

import com.teambind.co.kr.chatdding.adapter.in.web.dto.ApiResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.GetChatRoomDetailResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.GetChatRoomsResponse;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailResult;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailUseCase;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsResult;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅방 API Controller (Web Adapter)
 */
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final GetChatRoomsUseCase getChatRoomsUseCase;
    private final GetChatRoomDetailUseCase getChatRoomDetailUseCase;

    /**
     * 채팅방 목록 조회
     *
     * GET /api/v1/rooms
     */
    @GetMapping
    public ResponseEntity<ApiResponse<GetChatRoomsResponse>> getChatRooms(
            @RequestHeader("X-User-Id") Long userId
    ) {
        GetChatRoomsResult result = getChatRoomsUseCase.execute(
                GetChatRoomsQuery.of(userId)
        );

        return ResponseEntity.ok(ApiResponse.success(GetChatRoomsResponse.from(result)));
    }

    /**
     * 채팅방 상세 조회
     *
     * GET /api/v1/rooms/{roomId}
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<GetChatRoomDetailResponse>> getChatRoomDetail(
            @PathVariable String roomId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        GetChatRoomDetailResult result = getChatRoomDetailUseCase.execute(
                GetChatRoomDetailQuery.of(roomId, userId)
        );

        return ResponseEntity.ok(ApiResponse.success(GetChatRoomDetailResponse.from(result)));
    }
}
