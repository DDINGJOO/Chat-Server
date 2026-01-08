package com.teambind.co.kr.chatdding.adapter.in.web.controller;

import com.teambind.co.kr.chatdding.adapter.in.web.dto.ApiResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.request.CreateDmRequest;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.request.CreateGroupRequest;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.CreateDmResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.CreateGroupResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.GetChatRoomDetailResponse;
import com.teambind.co.kr.chatdding.adapter.in.web.dto.response.GetChatRoomsResponse;
import com.teambind.co.kr.chatdding.application.port.in.CreateDmResult;
import com.teambind.co.kr.chatdding.application.port.in.CreateDmUseCase;
import com.teambind.co.kr.chatdding.application.port.in.CreateGroupResult;
import com.teambind.co.kr.chatdding.application.port.in.CreateGroupUseCase;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailResult;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailUseCase;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsQuery;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsResult;
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsUseCase;
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

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;

/**
 * 채팅방 API Controller (Web Adapter)
 */
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final GetChatRoomsUseCase getChatRoomsUseCase;
    private final GetChatRoomDetailUseCase getChatRoomDetailUseCase;
    private final CreateDmUseCase createDmUseCase;
    private final CreateGroupUseCase createGroupUseCase;

    /**
     * 채팅방 목록 조회
     *
     * GET /api/v1/rooms
     * GET /api/v1/rooms?type=DM
     * GET /api/v1/rooms?type=PLACE_INQUIRY
     */
    @GetMapping
    public ResponseEntity<ApiResponse<GetChatRoomsResponse>> getChatRooms(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) ChatRoomType type
    ) {
        GetChatRoomsResult result = getChatRoomsUseCase.execute(
                GetChatRoomsQuery.of(userId, type)
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

    /**
     * DM 채팅방 생성
     *
     * POST /api/v1/rooms/dm
     */
    @PostMapping("/dm")
    public ResponseEntity<ApiResponse<CreateDmResponse>> createDm(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateDmRequest request
    ) {
        CreateDmResult result = createDmUseCase.execute(
                request.toCommand(userId)
        );

        HttpStatus status = result.isNewRoom() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity
                .status(status)
                .body(ApiResponse.success(CreateDmResponse.from(result)));
    }

    /**
     * 그룹 채팅방 생성
     *
     * POST /api/v1/rooms/group
     */
    @PostMapping("/group")
    public ResponseEntity<ApiResponse<CreateGroupResponse>> createGroup(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        CreateGroupResult result = createGroupUseCase.execute(
                request.toCommand(userId)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(CreateGroupResponse.from(result)));
    }
}
