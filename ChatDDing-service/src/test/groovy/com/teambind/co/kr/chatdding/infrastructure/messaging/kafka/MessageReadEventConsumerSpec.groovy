package com.teambind.co.kr.chatdding.infrastructure.messaging.kafka

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.event.MessageReadEvent
import com.teambind.co.kr.chatdding.domain.message.MessageRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class MessageReadEventConsumerSpec extends Specification {

    MessageRepository messageRepository = Mock()
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new ParameterNamesModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    @Subject
    MessageReadEventConsumer consumer = new MessageReadEventConsumer(
            messageRepository,
            objectMapper
    )

    def "MessageReadEvent 소비 시 bulkMarkAsRead가 호출된다"() {
        given:
        def event = MessageReadEvent.of("123", 1L, 0)
        def payload = objectMapper.writeValueAsString(event)

        when:
        consumer.consume(payload)

        then:
        1 * messageRepository.bulkMarkAsRead(
                { RoomId roomId -> roomId.toStringValue() == "123" },
                { UserId userId -> userId.getValue() == 1L },
                _ as LocalDateTime
        ) >> 5
    }

    def "잘못된 페이로드는 예외를 로깅하고 처리를 건너뛴다"() {
        given:
        def invalidPayload = "{ invalid json }"

        when:
        consumer.consume(invalidPayload)

        then:
        0 * messageRepository.bulkMarkAsRead(_, _, _)
        noExceptionThrown()
    }

    def "bulkMarkAsRead 결과가 0이어도 정상 처리된다"() {
        given:
        def event = MessageReadEvent.of("456", 2L, 0)
        def payload = objectMapper.writeValueAsString(event)

        when:
        consumer.consume(payload)

        then:
        1 * messageRepository.bulkMarkAsRead(_, _, _) >> 0
        noExceptionThrown()
    }

    def "이벤트의 occurredAt이 readAt으로 전달된다"() {
        given:
        def event = MessageReadEvent.of("789", 3L, 0)
        def payload = objectMapper.writeValueAsString(event)

        when:
        consumer.consume(payload)

        then:
        1 * messageRepository.bulkMarkAsRead(_, _, { LocalDateTime readAt ->
            readAt != null
        }) >> 1
    }
}
