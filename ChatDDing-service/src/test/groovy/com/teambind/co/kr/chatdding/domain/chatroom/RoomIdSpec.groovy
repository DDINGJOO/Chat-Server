package com.teambind.co.kr.chatdding.domain.chatroom

import com.teambind.co.kr.chatdding.domain.common.DomainId
import spock.lang.Specification
import spock.lang.Unroll

class RoomIdSpec extends Specification {

    def "RoomId.of()로 유효한 값으로 생성하면 성공한다"() {
        when:
        def roomId = RoomId.of(1234567890123456789L)

        then:
        roomId != null
        roomId.getValue() == 1234567890123456789L
    }

    @Unroll
    def "RoomId 생성 - 다양한 유효값: #value"() {
        when:
        def roomId = RoomId.of(value)

        then:
        roomId.getValue() == value

        where:
        value << [1L, 100L, Long.MAX_VALUE, 1234567890123456789L]
    }

    def "RoomId 생성 시 null 값이면 IllegalArgumentException 발생"() {
        when:
        RoomId.of(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "RoomId value cannot be null"
    }

    @Unroll
    def "RoomId 생성 시 0 또는 음수(#value)이면 IllegalArgumentException 발생"() {
        when:
        RoomId.of(value)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("RoomId value must be positive")

        where:
        value << [0L, -1L, -100L, Long.MIN_VALUE]
    }

    def "RoomId.fromString()으로 문자열에서 생성하면 성공한다"() {
        when:
        def roomId = RoomId.fromString("1234567890123456789")

        then:
        roomId.getValue() == 1234567890123456789L
    }

    def "RoomId.fromString() - null이면 IllegalArgumentException 발생"() {
        when:
        RoomId.fromString(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "RoomId string value cannot be null or blank"
    }

    @Unroll
    def "RoomId.fromString() - 빈 문자열('#value')이면 IllegalArgumentException 발생"() {
        when:
        RoomId.fromString(value)

        then:
        thrown(IllegalArgumentException)

        where:
        value << ["", "   ", "\t", "\n"]
    }

    def "RoomId.fromString() - 숫자가 아닌 문자열이면 NumberFormatException 발생"() {
        when:
        RoomId.fromString("not-a-number")

        then:
        thrown(NumberFormatException)
    }

    def "RoomId.fromString() - 음수 문자열이면 IllegalArgumentException 발생"() {
        when:
        RoomId.fromString("-123")

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("RoomId value must be positive")
    }

    def "동일한 값을 가진 RoomId는 equals가 true"() {
        given:
        def roomId1 = RoomId.of(123L)
        def roomId2 = RoomId.of(123L)

        expect:
        roomId1 == roomId2
        roomId1.hashCode() == roomId2.hashCode()
    }

    def "다른 값을 가진 RoomId는 equals가 false"() {
        given:
        def roomId1 = RoomId.of(123L)
        def roomId2 = RoomId.of(456L)

        expect:
        roomId1 != roomId2
    }

    def "toString()은 숫자 문자열을 반환한다"() {
        given:
        def roomId = RoomId.of(1234567890123456789L)

        expect:
        roomId.toString() == "1234567890123456789"
    }

    def "RoomId는 DomainId 인터페이스를 구현한다"() {
        given:
        def roomId = RoomId.of(1L)

        expect:
        roomId instanceof DomainId
    }

    def "RoomId.of()와 fromString()은 동일한 결과를 반환한다"() {
        given:
        def value = 9876543210L
        def roomIdFromOf = RoomId.of(value)
        def roomIdFromString = RoomId.fromString(value.toString())

        expect:
        roomIdFromOf == roomIdFromString
    }

    def "RoomId는 toStringValue() 메소드를 지원한다"() {
        given:
        def roomId = RoomId.of(12345L)

        expect:
        roomId.toStringValue() == "12345"
    }
}
