package com.teambind.co.kr.chatdding.adapter.in.web

import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import spock.lang.Specification
import spock.lang.Subject

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class GlobalExceptionHandlerSpec extends Specification {

    @Subject
    GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler()

    MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(exceptionHandler)
                .build()
    }

    def "ChatException 처리 - CHAT_ROOM_NOT_FOUND"() {
        when:
        def response = mockMvc.perform(get("/test/chat-exception/not-found"))

        then:
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("CHAT_001"))
                .andExpect(jsonPath('$.error.message').exists())
    }

    def "ChatException 처리 - CHAT_ROOM_ACCESS_DENIED"() {
        when:
        def response = mockMvc.perform(get("/test/chat-exception/access-denied"))

        then:
        response.andExpect(status().isForbidden())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("CHAT_002"))
    }

    def "ChatException 처리 - MESSAGE_NOT_FOUND"() {
        when:
        def response = mockMvc.perform(get("/test/chat-exception/message-not-found"))

        then:
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("CHAT_006"))
    }

    def "ChatException 처리 - MESSAGE_CONTENT_EMPTY"() {
        when:
        def response = mockMvc.perform(get("/test/chat-exception/content-empty"))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("CHAT_004"))
    }

    def "MethodArgumentNotValidException 처리"() {
        when:
        def response = mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"name": ""}'))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("VALIDATION_ERROR"))
                .andExpect(jsonPath('$.error.message').exists())
    }

    def "MissingRequestHeaderException 처리"() {
        when:
        def response = mockMvc.perform(get("/test/missing-header"))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("MISSING_HEADER"))
                .andExpect(jsonPath('$.error.message').value("필수 헤더가 누락되었습니다: X-User-Id"))
    }

    def "IllegalArgumentException 처리"() {
        when:
        def response = mockMvc.perform(get("/test/illegal-argument"))

        then:
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("BAD_REQUEST"))
                .andExpect(jsonPath('$.error.message').value("잘못된 인자입니다"))
    }

    def "일반 Exception 처리"() {
        when:
        def response = mockMvc.perform(get("/test/unexpected-error"))

        then:
        response.andExpect(status().isInternalServerError())
                .andExpect(jsonPath('$.success').value(false))
                .andExpect(jsonPath('$.error.code').value("INTERNAL_ERROR"))
                .andExpect(jsonPath('$.error.message').value("서버 내부 오류가 발생했습니다"))
    }

    def "ChatException 직접 호출 테스트"() {
        given:
        def exception = new ChatException(ErrorCode.CHAT_ROOM_NOT_FOUND)

        when:
        def result = exceptionHandler.handleChatException(exception)

        then:
        result.statusCode.value() == 404
        result.body.success() == false
        result.body.error().code() == "CHAT_001"
    }

    def "IllegalArgumentException 직접 호출 테스트"() {
        given:
        def exception = new IllegalArgumentException("테스트 오류")

        when:
        def result = exceptionHandler.handleIllegalArgumentException(exception)

        then:
        result.statusCode.value() == 400
        result.body.success() == false
        result.body.error().code() == "BAD_REQUEST"
        result.body.error().message() == "테스트 오류"
    }

    def "일반 Exception 직접 호출 테스트"() {
        given:
        def exception = new RuntimeException("예상치 못한 오류")

        when:
        def result = exceptionHandler.handleException(exception)

        then:
        result.statusCode.value() == 500
        result.body.success() == false
        result.body.error().code() == "INTERNAL_ERROR"
    }

    @RestController
    static class TestController {

        @GetMapping("/test/chat-exception/not-found")
        void chatRoomNotFound() {
            throw new ChatException(ErrorCode.CHAT_ROOM_NOT_FOUND)
        }

        @GetMapping("/test/chat-exception/access-denied")
        void accessDenied() {
            throw new ChatException(ErrorCode.CHAT_ROOM_ACCESS_DENIED)
        }

        @GetMapping("/test/chat-exception/message-not-found")
        void messageNotFound() {
            throw new ChatException(ErrorCode.MESSAGE_NOT_FOUND)
        }

        @GetMapping("/test/chat-exception/content-empty")
        void contentEmpty() {
            throw new ChatException(ErrorCode.MESSAGE_CONTENT_EMPTY)
        }

        @PostMapping("/test/validation")
        void validation(@Valid @RequestBody TestRequest request) {
            // validation test
        }

        @GetMapping("/test/missing-header")
        void missingHeader(@RequestHeader("X-User-Id") Long userId) {
            // missing header test
        }

        @GetMapping("/test/illegal-argument")
        void illegalArgument() {
            throw new IllegalArgumentException("잘못된 인자입니다")
        }

        @GetMapping("/test/unexpected-error")
        void unexpectedError() {
            throw new RuntimeException("예상치 못한 오류")
        }

        static class TestRequest {
            @NotBlank(message = "이름은 필수입니다")
            String name
        }
    }
}
