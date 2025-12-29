package com.teambind.co.kr.chatdding.adapter.in.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.teambind.co.kr.chatdding.adapter.in.web.GlobalExceptionHandler
import com.teambind.co.kr.chatdding.application.port.in.AssignSupportAgentResult
import com.teambind.co.kr.chatdding.application.port.in.AssignSupportAgentUseCase
import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatResult
import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatUseCase
import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestResult
import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestUseCase
import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueResult
import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueUseCase
import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SupportControllerSpec extends Specification {

    CreateSupportRequestUseCase createSupportRequestUseCase = Mock()
    GetSupportQueueUseCase getSupportQueueUseCase = Mock()
    AssignSupportAgentUseCase assignSupportAgentUseCase = Mock()
    CloseSupportChatUseCase closeSupportChatUseCase = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    @Subject
    SupportController supportController

    MockMvc mockMvc

    def setup() {
        supportController = new SupportController(
                createSupportRequestUseCase,
                getSupportQueueUseCase,
                assignSupportAgentUseCase,
                closeSupportChatUseCase
        )
        mockMvc = MockMvcBuilders.standaloneSetup(supportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    // ========================
    // POST /api/v1/chat/support
    // ========================

    def "상담 요청 생성 성공 시 201 Created 반환"() {
        given:
        def userId = 100L
        def result = new CreateSupportRequestResult("123", "ACTIVE", "결제 문의", LocalDateTime.now())

        when:
        def response = mockMvc.perform(post("/api/v1/chat/support")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"category": "결제 문의"}'))

        then:
        1 * createSupportRequestUseCase.execute(_) >> result

        and:
        response.andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.roomId').value("123"))
                .andExpect(jsonPath('$.data.status').value("ACTIVE"))
                .andExpect(jsonPath('$.data.category').value("결제 문의"))
    }

    def "카테고리 없이 상담 요청 생성 성공"() {
        given:
        def userId = 100L
        def result = new CreateSupportRequestResult("123", "ACTIVE", null, LocalDateTime.now())

        when:
        def response = mockMvc.perform(post("/api/v1/chat/support")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{}'))

        then:
        1 * createSupportRequestUseCase.execute(_) >> result

        and:
        response.andExpect(status().isCreated())
                .andExpect(jsonPath('$.data.category').isEmpty())
    }

    def "X-User-Id 헤더 누락 시 400 Bad Request 반환"() {
        when:
        def response = mockMvc.perform(post("/api/v1/chat/support")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{}'))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.error.code').value("MISSING_HEADER"))
    }

    // ========================
    // GET /api/v1/chat/support/queue
    // ========================

    def "상담 대기열 조회 성공"() {
        given:
        def item = new GetSupportQueueResult.SupportQueueItem("123", 100L, "결제 문의", LocalDateTime.now(), 15L)
        def result = GetSupportQueueResult.of([item], null, 1L)

        when:
        def response = mockMvc.perform(get("/api/v1/chat/support/queue"))

        then:
        1 * getSupportQueueUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.items').isArray())
                .andExpect(jsonPath('$.data.items.length()').value(1))
                .andExpect(jsonPath('$.data.items[0].roomId').value("123"))
                .andExpect(jsonPath('$.data.items[0].userId').value(100))
                .andExpect(jsonPath('$.data.items[0].category').value("결제 문의"))
                .andExpect(jsonPath('$.data.items[0].waitingMinutes').value(15))
                .andExpect(jsonPath('$.data.totalCount').value(1))
    }

    def "빈 대기열 조회"() {
        given:
        def result = GetSupportQueueResult.of([], null, 0L)

        when:
        def response = mockMvc.perform(get("/api/v1/chat/support/queue"))

        then:
        1 * getSupportQueueUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.data.items').isEmpty())
                .andExpect(jsonPath('$.data.totalCount').value(0))
    }

    def "페이지네이션 파라미터 전달"() {
        given:
        def result = GetSupportQueueResult.of([], null, 0L)

        when:
        def response = mockMvc.perform(get("/api/v1/chat/support/queue")
                .param("cursor", "100")
                .param("limit", "10"))

        then:
        1 * getSupportQueueUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
    }

    // ========================
    // POST /api/v1/chat/support/{roomId}/assign
    // ========================

    def "상담원 배정 성공"() {
        given:
        def agentId = 999L
        def roomId = "123"
        def result = new AssignSupportAgentResult(roomId, agentId, LocalDateTime.now())

        when:
        def response = mockMvc.perform(post("/api/v1/chat/support/${roomId}/assign")
                .header("X-Agent-Id", agentId))

        then:
        1 * assignSupportAgentUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.roomId').value("123"))
                .andExpect(jsonPath('$.data.agentId').value(999))
    }

    def "존재하지 않는 채팅방에 상담원 배정 시 404 반환"() {
        given:
        def agentId = 999L
        def roomId = "999"

        when:
        def response = mockMvc.perform(post("/api/v1/chat/support/${roomId}/assign")
                .header("X-Agent-Id", agentId))

        then:
        1 * assignSupportAgentUseCase.execute(_) >> { throw new ChatException(ErrorCode.ROOM_NOT_FOUND) }

        and:
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath('$.success').value(false))
    }

    def "이미 종료된 채팅방에 상담원 배정 시 400 반환"() {
        given:
        def agentId = 999L
        def roomId = "123"

        when:
        def response = mockMvc.perform(post("/api/v1/chat/support/${roomId}/assign")
                .header("X-Agent-Id", agentId))

        then:
        1 * assignSupportAgentUseCase.execute(_) >> { throw new ChatException(ErrorCode.ROOM_ALREADY_CLOSED) }

        and:
        response.andExpect(status().isConflict())
                .andExpect(jsonPath('$.success').value(false))
    }

    def "X-Agent-Id 헤더 누락 시 400 Bad Request 반환"() {
        when:
        def response = mockMvc.perform(post("/api/v1/chat/support/123/assign"))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.error.code').value("MISSING_HEADER"))
    }

    // ========================
    // POST /api/v1/chat/support/{roomId}/close
    // ========================

    def "상담 종료 성공"() {
        given:
        def userId = 100L
        def roomId = "123"
        def result = new CloseSupportChatResult(roomId, LocalDateTime.now())

        when:
        def response = mockMvc.perform(post("/api/v1/chat/support/${roomId}/close")
                .header("X-User-Id", userId))

        then:
        1 * closeSupportChatUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.roomId').value("123"))
    }

    def "존재하지 않는 채팅방 종료 시 404 반환"() {
        given:
        def userId = 100L
        def roomId = "999"

        when:
        def response = mockMvc.perform(post("/api/v1/chat/support/${roomId}/close")
                .header("X-User-Id", userId))

        then:
        1 * closeSupportChatUseCase.execute(_) >> { throw new ChatException(ErrorCode.ROOM_NOT_FOUND) }

        and:
        response.andExpect(status().isNotFound())
    }

    def "참여자가 아닌 사용자가 종료 시도 시 403 반환"() {
        given:
        def userId = 888L
        def roomId = "123"

        when:
        def response = mockMvc.perform(post("/api/v1/chat/support/${roomId}/close")
                .header("X-User-Id", userId))

        then:
        1 * closeSupportChatUseCase.execute(_) >> { throw new ChatException(ErrorCode.NOT_PARTICIPANT) }

        and:
        response.andExpect(status().isForbidden())
    }

    def "X-User-Id 헤더 누락 시 종료 400 반환"() {
        when:
        def response = mockMvc.perform(post("/api/v1/chat/support/123/close"))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.error.code').value("MISSING_HEADER"))
    }
}
