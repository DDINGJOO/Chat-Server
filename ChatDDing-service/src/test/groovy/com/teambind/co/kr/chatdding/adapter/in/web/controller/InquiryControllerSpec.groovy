package com.teambind.co.kr.chatdding.adapter.in.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.teambind.co.kr.chatdding.adapter.in.web.GlobalExceptionHandler
import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryResult
import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryUseCase
import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesResult
import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesUseCase
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

class InquiryControllerSpec extends Specification {

    CreatePlaceInquiryUseCase createPlaceInquiryUseCase = Mock()
    GetHostInquiriesUseCase getHostInquiriesUseCase = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    @Subject
    InquiryController inquiryController

    MockMvc mockMvc

    def setup() {
        inquiryController = new InquiryController(createPlaceInquiryUseCase, getHostInquiriesUseCase)
        mockMvc = MockMvcBuilders.standaloneSetup(inquiryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    // ========================
    // POST /api/v1/chat/inquiry
    // ========================

    def "공간 문의 생성 성공 시 201 Created 반환"() {
        given:
        def userId = 100L
        def contextDto = new CreatePlaceInquiryResult.ContextDto("PLACE", 12345L, "강남 스터디룸 A")
        def result = new CreatePlaceInquiryResult("999", "PLACE_INQUIRY", contextDto, LocalDateTime.now())

        when:
        def response = mockMvc.perform(post("/api/v1/chat/inquiry")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"placeId": 12345, "placeName": "강남 스터디룸 A", "hostId": 200}'))

        then:
        1 * createPlaceInquiryUseCase.execute(_) >> result

        and:
        response.andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.roomId').value("999"))
                .andExpect(jsonPath('$.data.type').value("PLACE_INQUIRY"))
                .andExpect(jsonPath('$.data.context.contextType').value("PLACE"))
                .andExpect(jsonPath('$.data.context.contextId').value(12345))
                .andExpect(jsonPath('$.data.context.contextName').value("강남 스터디룸 A"))
    }

    def "초기 메시지 포함하여 공간 문의 생성"() {
        given:
        def userId = 100L
        def contextDto = new CreatePlaceInquiryResult.ContextDto("PLACE", 12345L, "강남 스터디룸 A")
        def result = new CreatePlaceInquiryResult("999", "PLACE_INQUIRY", contextDto, LocalDateTime.now())

        when:
        def response = mockMvc.perform(post("/api/v1/chat/inquiry")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"placeId": 12345, "placeName": "강남 스터디룸 A", "hostId": 200, "initialMessage": "예약 문의드립니다"}'))

        then:
        1 * createPlaceInquiryUseCase.execute(_) >> result

        and:
        response.andExpect(status().isCreated())
    }

    def "placeId 누락 시 400 Bad Request 반환"() {
        given:
        def userId = 100L

        when:
        def response = mockMvc.perform(post("/api/v1/chat/inquiry")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"placeName": "강남 스터디룸 A", "hostId": 200}'))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
    }

    def "hostId 누락 시 400 Bad Request 반환"() {
        given:
        def userId = 100L

        when:
        def response = mockMvc.perform(post("/api/v1/chat/inquiry")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"placeId": 12345, "placeName": "강남 스터디룸 A"}'))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
    }

    def "X-User-Id 헤더 누락 시 400 Bad Request 반환"() {
        when:
        def response = mockMvc.perform(post("/api/v1/chat/inquiry")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"placeId": 12345, "placeName": "강남 스터디룸 A", "hostId": 200}'))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.error.code').value("MISSING_HEADER"))
    }

    def "중복 문의 시 409 Conflict 반환"() {
        given:
        def userId = 100L

        when:
        def response = mockMvc.perform(post("/api/v1/chat/inquiry")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"placeId": 12345, "placeName": "강남 스터디룸 A", "hostId": 200}'))

        then:
        1 * createPlaceInquiryUseCase.execute(_) >> { throw new ChatException(ErrorCode.DUPLICATE_INQUIRY) }

        and:
        response.andExpect(status().isConflict())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("CHAT_011"))
    }

    // ========================
    // GET /api/v1/chat/inquiry/host
    // ========================

    def "호스트 문의 목록 조회 성공"() {
        given:
        def hostId = 200L
        def contextDto = new GetHostInquiriesResult.ContextDto("PLACE", 12345L, "강남 스터디룸 A")
        def inquiryItem = new GetHostInquiriesResult.InquiryItem(
                "999", 100L, "게스트닉네임", contextDto, "마지막 메시지", 3, LocalDateTime.now()
        )
        def result = new GetHostInquiriesResult([inquiryItem], null, false)

        when:
        def response = mockMvc.perform(get("/api/v1/chat/inquiry/host")
                .header("X-User-Id", hostId))

        then:
        1 * getHostInquiriesUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.inquiries').isArray())
                .andExpect(jsonPath('$.data.inquiries.length()').value(1))
                .andExpect(jsonPath('$.data.inquiries[0].roomId').value("999"))
                .andExpect(jsonPath('$.data.inquiries[0].guestId').value(100))
                .andExpect(jsonPath('$.data.inquiries[0].unreadCount').value(3))
                .andExpect(jsonPath('$.data.hasMore').value(false))
    }

    def "placeId 필터로 문의 목록 조회"() {
        given:
        def hostId = 200L
        def result = GetHostInquiriesResult.empty()

        when:
        def response = mockMvc.perform(get("/api/v1/chat/inquiry/host")
                .header("X-User-Id", hostId)
                .param("placeId", "12345"))

        then:
        1 * getHostInquiriesUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
    }

    def "빈 문의 목록 조회"() {
        given:
        def hostId = 200L
        def result = GetHostInquiriesResult.empty()

        when:
        def response = mockMvc.perform(get("/api/v1/chat/inquiry/host")
                .header("X-User-Id", hostId))

        then:
        1 * getHostInquiriesUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.data.inquiries').isEmpty())
                .andExpect(jsonPath('$.data.hasMore').value(false))
    }

    def "페이지네이션 파라미터 전달"() {
        given:
        def hostId = 200L
        def result = GetHostInquiriesResult.empty()

        when:
        def response = mockMvc.perform(get("/api/v1/chat/inquiry/host")
                .header("X-User-Id", hostId)
                .param("cursor", "abc123")
                .param("limit", "10"))

        then:
        1 * getHostInquiriesUseCase.execute(_) >> result

        and:
        response.andExpect(status().isOk())
    }
}
