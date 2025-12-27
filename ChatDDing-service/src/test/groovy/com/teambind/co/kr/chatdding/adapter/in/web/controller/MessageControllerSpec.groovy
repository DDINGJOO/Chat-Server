package com.teambind.co.kr.chatdding.adapter.in.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.teambind.co.kr.chatdding.adapter.in.web.GlobalExceptionHandler
import com.teambind.co.kr.chatdding.application.port.in.GetMessagesResult
import com.teambind.co.kr.chatdding.application.port.in.GetMessagesUseCase
import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadResult
import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadUseCase
import com.teambind.co.kr.chatdding.application.port.in.SendMessageResult
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase
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

class MessageControllerSpec extends Specification {

    SendMessageUseCase sendMessageUseCase = Mock()
    GetMessagesUseCase getMessagesUseCase = Mock()
    MarkAsReadUseCase markAsReadUseCase = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    @Subject
    MessageController messageController

    MockMvc mockMvc

    def setup() {
        messageController = new MessageController(sendMessageUseCase, getMessagesUseCase, markAsReadUseCase)
        mockMvc = MockMvcBuilders.standaloneSetup(messageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    def "메시지 전송 성공 시 201 Created 반환"() {
        given:
        def roomId = "123"
        def userId = 100L
        def content = "안녕하세요"
        def result = new SendMessageResult("msg1", roomId, userId, content, LocalDateTime.now())

        when:
        def response = mockMvc.perform(post("/api/v1/rooms/{roomId}/messages", roomId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"content": "안녕하세요"}'))

        then:
        1 * sendMessageUseCase.execute(_) >> result

        and:
        response.andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.messageId').value("msg1"))
                .andExpect(jsonPath('$.data.roomId').value(roomId))
                .andExpect(jsonPath('$.data.senderId').value(userId))
                .andExpect(jsonPath('$.data.content').value(content))
    }

    def "메시지 내용이 없으면 400 Bad Request 반환"() {
        given:
        def roomId = "123"
        def userId = 100L

        when:
        def response = mockMvc.perform(post("/api/v1/rooms/{roomId}/messages", roomId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"content": ""}'))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("VALIDATION_ERROR"))
    }

    def "X-User-Id 헤더 누락 시 400 Bad Request 반환"() {
        given:
        def roomId = "123"

        when:
        def response = mockMvc.perform(post("/api/v1/rooms/{roomId}/messages", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"content": "안녕하세요"}'))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("MISSING_HEADER"))
    }

    def "존재하지 않는 채팅방에 메시지 전송 시 404 반환"() {
        given:
        def roomId = "999"
        def userId = 100L

        sendMessageUseCase.execute(_) >> { throw new ChatException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

        when:
        def response = mockMvc.perform(post("/api/v1/rooms/{roomId}/messages", roomId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"content": "테스트"}'))

        then:
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("CHAT_001"))
    }

    def "참여하지 않은 채팅방에 메시지 전송 시 403 반환"() {
        given:
        def roomId = "123"
        def userId = 999L

        sendMessageUseCase.execute(_) >> { throw new ChatException(ErrorCode.CHAT_ROOM_ACCESS_DENIED) }

        when:
        def response = mockMvc.perform(post("/api/v1/rooms/{roomId}/messages", roomId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"content": "테스트"}'))

        then:
        response.andExpect(status().isForbidden())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("CHAT_002"))
    }

    def "메시지 목록 조회 성공"() {
        given:
        def roomId = "123"
        def userId = 100L
        def messages = [
                new GetMessagesResult.MessageItem("msg1", roomId, userId, "메시지1", 1, LocalDateTime.now()),
                new GetMessagesResult.MessageItem("msg2", roomId, userId, "메시지2", 2, LocalDateTime.now())
        ]
        def result = new GetMessagesResult(messages, "msg1", true)

        when:
        def response = mockMvc.perform(get("/api/v1/rooms/{roomId}/messages", roomId)
                .header("X-User-Id", userId))

        then:
        1 * getMessagesUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.messages').isArray())
                .andExpect(jsonPath('$.data.messages.length()').value(2))
                .andExpect(jsonPath('$.data.hasMore').value(true))
                .andExpect(jsonPath('$.data.nextCursor').value("msg1"))
    }

    def "메시지 목록 조회 시 cursor와 limit 파라미터 전달"() {
        given:
        def roomId = "123"
        def userId = 100L
        def result = new GetMessagesResult([], null, false)

        getMessagesUseCase.execute(_) >> result

        when:
        mockMvc.perform(get("/api/v1/rooms/{roomId}/messages", roomId)
                .header("X-User-Id", userId)
                .param("cursor", "msg10")
                .param("limit", "20"))

        then:
        noExceptionThrown()
    }

    def "빈 메시지 목록 조회"() {
        given:
        def roomId = "123"
        def userId = 100L
        def result = new GetMessagesResult([], null, false)

        when:
        def response = mockMvc.perform(get("/api/v1/rooms/{roomId}/messages", roomId)
                .header("X-User-Id", userId))

        then:
        1 * getMessagesUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.data.messages').isEmpty())
                .andExpect(jsonPath('$.data.hasMore').value(false))
    }

    def "읽음 처리 성공"() {
        given:
        def roomId = "123"
        def userId = 100L
        def result = new MarkAsReadResult(roomId, userId, LocalDateTime.now(), 5)

        when:
        def response = mockMvc.perform(post("/api/v1/rooms/{roomId}/messages/read", roomId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))

        then:
        1 * markAsReadUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.roomId').value(roomId))
                .andExpect(jsonPath('$.data.userId').value(userId))
                .andExpect(jsonPath('$.data.readCount').value(5))
    }

    def "특정 메시지까지 읽음 처리"() {
        given:
        def roomId = "123"
        def userId = 100L
        def result = new MarkAsReadResult(roomId, userId, LocalDateTime.now(), 3)

        markAsReadUseCase.execute(_) >> result

        when:
        def response = mockMvc.perform(post("/api/v1/rooms/{roomId}/messages/read", roomId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"lastMessageId": "5"}'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.data.readCount').value(3))
    }

    def "읽음 처리 시 존재하지 않는 채팅방이면 404 반환"() {
        given:
        def roomId = "999"
        def userId = 100L

        markAsReadUseCase.execute(_) >> { throw new ChatException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

        when:
        def response = mockMvc.perform(post("/api/v1/rooms/{roomId}/messages/read", roomId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))

        then:
        response.andExpect(status().isNotFound())
    }

    def "메시지 내용이 5000자를 초과하면 400 Bad Request 반환"() {
        given:
        def roomId = "123"
        def userId = 100L
        def longContent = "가" * 5001

        when:
        def response = mockMvc.perform(post("/api/v1/rooms/{roomId}/messages", roomId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"${longContent}\"}"))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.error.code').value("VALIDATION_ERROR"))
    }
}
