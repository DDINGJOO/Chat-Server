package com.teambind.co.kr.chatdding.domain.message

import com.teambind.co.kr.chatdding.domain.common.DomainId
import spock.lang.Specification
import spock.lang.Unroll

class MessageIdSpec extends Specification {

    def "MessageId.of()로 유효한 값으로 생성하면 성공한다"() {
        when:
        def messageId = MessageId.of(1234567890123456789L)

        then:
        messageId != null
        messageId.getValue() == 1234567890123456789L
    }

    @Unroll
    def "MessageId 생성 - 다양한 유효값: #value"() {
        when:
        def messageId = MessageId.of(value)

        then:
        messageId.getValue() == value

        where:
        value << [1L, 100L, Long.MAX_VALUE, 1234567890123456789L]
    }

    def "MessageId 생성 시 null 값이면 IllegalArgumentException 발생"() {
        when:
        MessageId.of(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "MessageId value cannot be null"
    }

    @Unroll
    def "MessageId 생성 시 0 또는 음수(#value)이면 IllegalArgumentException 발생"() {
        when:
        MessageId.of(value)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("MessageId value must be positive")

        where:
        value << [0L, -1L, -100L, Long.MIN_VALUE]
    }

    def "MessageId.fromString()으로 문자열에서 생성하면 성공한다"() {
        when:
        def messageId = MessageId.fromString("1234567890123456789")

        then:
        messageId.getValue() == 1234567890123456789L
    }

    def "MessageId.fromString() - null이면 IllegalArgumentException 발생"() {
        when:
        MessageId.fromString(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "MessageId string value cannot be null or blank"
    }

    @Unroll
    def "MessageId.fromString() - 빈 문자열('#value')이면 IllegalArgumentException 발생"() {
        when:
        MessageId.fromString(value)

        then:
        thrown(IllegalArgumentException)

        where:
        value << ["", "   ", "\t", "\n"]
    }

    def "MessageId.fromString() - 숫자가 아닌 문자열이면 NumberFormatException 발생"() {
        when:
        MessageId.fromString("abc123")

        then:
        thrown(NumberFormatException)
    }

    def "MessageId.fromString() - 음수 문자열이면 IllegalArgumentException 발생"() {
        when:
        MessageId.fromString("-999")

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("MessageId value must be positive")
    }

    def "동일한 값을 가진 MessageId는 equals가 true"() {
        given:
        def messageId1 = MessageId.of(123L)
        def messageId2 = MessageId.of(123L)

        expect:
        messageId1 == messageId2
        messageId1.hashCode() == messageId2.hashCode()
    }

    def "다른 값을 가진 MessageId는 equals가 false"() {
        given:
        def messageId1 = MessageId.of(123L)
        def messageId2 = MessageId.of(456L)

        expect:
        messageId1 != messageId2
    }

    def "toString()은 숫자 문자열을 반환한다"() {
        given:
        def messageId = MessageId.of(1234567890123456789L)

        expect:
        messageId.toString() == "1234567890123456789"
    }

    def "MessageId는 DomainId 인터페이스를 구현한다"() {
        given:
        def messageId = MessageId.of(1L)

        expect:
        messageId instanceof DomainId
    }

    def "MessageId.of()와 fromString()은 동일한 결과를 반환한다"() {
        given:
        def value = 9876543210L
        def messageIdFromOf = MessageId.of(value)
        def messageIdFromString = MessageId.fromString(value.toString())

        expect:
        messageIdFromOf == messageIdFromString
    }

    def "MessageId는 toStringValue() 메소드를 지원한다"() {
        given:
        def messageId = MessageId.of(12345L)

        expect:
        messageId.toStringValue() == "12345"
    }
}
