package com.teambind.co.kr.chatdding.domain.common

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class UserIdSpec extends Specification {

    def "UserId.of()로 유효한 값으로 생성하면 성공한다"() {
        when:
        def userId = UserId.of(1L)

        then:
        userId != null
        userId.getValue() == 1L
        userId.value() == 1L
    }

    @Unroll
    def "UserId 생성 - 다양한 유효값: #value"() {
        when:
        def userId = UserId.of(value)

        then:
        userId.getValue() == value

        where:
        value << [1L, 100L, Long.MAX_VALUE, 999_999_999L]
    }

    def "UserId 생성 시 null 값이면 IllegalArgumentException 발생"() {
        when:
        UserId.of(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "UserId value cannot be null"
    }

    @Unroll
    def "UserId 생성 시 0 또는 음수(#value)이면 IllegalArgumentException 발생"() {
        when:
        UserId.of(value)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("UserId value must be positive")

        where:
        value << [0L, -1L, -100L, Long.MIN_VALUE]
    }

    def "동일한 값을 가진 UserId는 equals가 true"() {
        given:
        def userId1 = UserId.of(123L)
        def userId2 = UserId.of(123L)

        expect:
        userId1 == userId2
        userId1.hashCode() == userId2.hashCode()
    }

    def "다른 값을 가진 UserId는 equals가 false"() {
        given:
        def userId1 = UserId.of(123L)
        def userId2 = UserId.of(456L)

        expect:
        userId1 != userId2
    }

    def "toString()은 숫자 문자열을 반환한다"() {
        given:
        def userId = UserId.of(12345L)

        expect:
        userId.toString() == "12345"
    }

    def "UserId는 DomainId 인터페이스를 구현한다"() {
        given:
        def userId = UserId.of(1L)

        expect:
        userId instanceof DomainId
    }

    def "UserId는 toStringValue() 메소드를 지원한다"() {
        given:
        def userId = UserId.of(12345L)

        expect:
        userId.toStringValue() == "12345"
    }
}
