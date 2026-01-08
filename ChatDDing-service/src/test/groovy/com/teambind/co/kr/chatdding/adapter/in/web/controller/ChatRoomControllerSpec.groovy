package com.teambind.co.kr.chatdding.adapter.in.web.controller

import com.teambind.co.kr.chatdding.adapter.in.web.GlobalExceptionHandler
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailResult
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailUseCase
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsResult
import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsUseCase
import com.teambind.co.kr.chatdding.application.port.in.CreateDmUseCase
import com.teambind.co.kr.chatdding.application.port.in.CreateGroupUseCase
import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ChatRoomControllerSpec extends Specification {

    GetChatRoomsUseCase getChatRoomsUseCase = Mock()
    GetChatRoomDetailUseCase getChatRoomDetailUseCase = Mock()
    CreateDmUseCase createDmUseCase = Mock()
    CreateGroupUseCase createGroupUseCase = Mock()

    @Subject
    ChatRoomController chatRoomController

    MockMvc mockMvc

    def setup() {
        chatRoomController = new ChatRoomController(getChatRoomsUseCase, getChatRoomDetailUseCase, createDmUseCase, createGroupUseCase)
        mockMvc = MockMvcBuilders.standaloneSetup(chatRoomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    def "채팅방 목록 조회 성공"() {
        given:
        def userId = 100L
        def chatRooms = [
                new GetChatRoomsResult.ChatRoomItem(
                        "1001",
                        ChatRoomType.DM,
                        "User2",
                        [100L, 200L],
                        "마지막 메시지",
                        LocalDateTime.now(),
                        3,
                        null
                ),
                new GetChatRoomsResult.ChatRoomItem(
                        "1002",
                        ChatRoomType.GROUP,
                        "개발팀",
                        [100L, 200L, 300L],
                        "안녕하세요",
                        LocalDateTime.now(),
                        0,
                        null
                )
        ]
        def result = new GetChatRoomsResult(chatRooms)

        when:
        def response = mockMvc.perform(get("/api/v1/rooms")
                .header("X-User-Id", userId))

        then:
        1 * getChatRoomsUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.chatRooms').isArray())
                .andExpect(jsonPath('$.data.chatRooms.length()').value(2))
                .andExpect(jsonPath('$.data.chatRooms[0].roomId').value("1001"))
                .andExpect(jsonPath('$.data.chatRooms[0].type').value("DM"))
                .andExpect(jsonPath('$.data.chatRooms[0].unreadCount').value(3))
    }

    def "채팅방 목록이 비어있는 경우"() {
        given:
        def userId = 100L
        def result = new GetChatRoomsResult([])

        when:
        def response = mockMvc.perform(get("/api/v1/rooms")
                .header("X-User-Id", userId))

        then:
        1 * getChatRoomsUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.data.chatRooms').isEmpty())
    }

    def "채팅방 목록 조회 시 X-User-Id 헤더 누락"() {
        when:
        def response = mockMvc.perform(get("/api/v1/rooms"))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("MISSING_HEADER"))
    }

    def "채팅방 상세 조회 성공"() {
        given:
        def roomId = "1001"
        def userId = 100L
        def now = LocalDateTime.now()
        def participants = [
                new GetChatRoomDetailResult.ParticipantInfo(100L, true, now, now),
                new GetChatRoomDetailResult.ParticipantInfo(200L, true, now, now)
        ]
        def result = new GetChatRoomDetailResult(
                roomId,
                ChatRoomType.DM,
                "User2",
                participants,
                100L,
                ChatRoomStatus.ACTIVE,
                now,
                now,
                0
        )

        getChatRoomDetailUseCase.execute(_) >> result

        when:
        def response = mockMvc.perform(get("/api/v1/rooms/{roomId}", roomId)
                .header("X-User-Id", userId))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.roomId').value(roomId))
                .andExpect(jsonPath('$.data.type').value("DM"))
                .andExpect(jsonPath('$.data.status').value("ACTIVE"))
                .andExpect(jsonPath('$.data.participants').isArray())
                .andExpect(jsonPath('$.data.participants.length()').value(2))
    }

    def "그룹 채팅방 상세 조회"() {
        given:
        def roomId = "1002"
        def userId = 100L
        def now = LocalDateTime.now()
        def participants = [
                new GetChatRoomDetailResult.ParticipantInfo(100L, true, now, now),
                new GetChatRoomDetailResult.ParticipantInfo(200L, true, now, now),
                new GetChatRoomDetailResult.ParticipantInfo(300L, false, null, now)
        ]
        def result = new GetChatRoomDetailResult(
                roomId,
                ChatRoomType.GROUP,
                "개발팀",
                participants,
                100L,
                ChatRoomStatus.ACTIVE,
                now,
                now,
                5
        )

        getChatRoomDetailUseCase.execute(_) >> result

        when:
        def response = mockMvc.perform(get("/api/v1/rooms/{roomId}", roomId)
                .header("X-User-Id", userId))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.data.type').value("GROUP"))
                .andExpect(jsonPath('$.data.name').value("개발팀"))
                .andExpect(jsonPath('$.data.participants.length()').value(3))
    }

    def "존재하지 않는 채팅방 상세 조회 시 404 반환"() {
        given:
        def roomId = "9999"
        def userId = 100L

        when:
        def response = mockMvc.perform(get("/api/v1/rooms/{roomId}", roomId)
                .header("X-User-Id", userId))

        then:
        1 * getChatRoomDetailUseCase.execute(_) >> { throw new ChatException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

        and:
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("CHAT_001"))
    }

    def "참여하지 않은 채팅방 상세 조회 시 403 반환"() {
        given:
        def roomId = "1001"
        def userId = 999L

        when:
        def response = mockMvc.perform(get("/api/v1/rooms/{roomId}", roomId)
                .header("X-User-Id", userId))

        then:
        1 * getChatRoomDetailUseCase.execute(_) >> { throw new ChatException(ErrorCode.CHAT_ROOM_ACCESS_DENIED) }

        and:
        response.andExpect(status().isForbidden())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("CHAT_002"))
    }

    def "채팅방 상세 조회 시 X-User-Id 헤더 누락"() {
        given:
        def roomId = "1001"

        when:
        def response = mockMvc.perform(get("/api/v1/rooms/{roomId}", roomId))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.error.code').value("MISSING_HEADER"))
    }

    def "상담 채팅방 상세 조회"() {
        given:
        def roomId = "2001"
        def userId = 100L
        def now = LocalDateTime.now()
        def participants = [
                new GetChatRoomDetailResult.ParticipantInfo(100L, true, now, now),
                new GetChatRoomDetailResult.ParticipantInfo(500L, true, now, now)
        ]
        def result = new GetChatRoomDetailResult(
                roomId,
                ChatRoomType.SUPPORT,
                "고객센터 상담",
                participants,
                500L,
                ChatRoomStatus.ACTIVE,
                now,
                now,
                0
        )

        getChatRoomDetailUseCase.execute(_) >> result

        when:
        def response = mockMvc.perform(get("/api/v1/rooms/{roomId}", roomId)
                .header("X-User-Id", userId))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.data.type').value("SUPPORT"))
                .andExpect(jsonPath('$.data.ownerId').value(500))
    }
}
