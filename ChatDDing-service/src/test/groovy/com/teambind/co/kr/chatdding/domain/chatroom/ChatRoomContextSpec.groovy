package com.teambind.co.kr.chatdding.domain.chatroom

import spock.lang.Specification

class ChatRoomContextSpec extends Specification {

    def "forPlace()로 공간 컨텍스트를 생성할 수 있다"() {
        when:
        def context = ChatRoomContext.forPlace(12345L, "강남 스터디룸 A")

        then:
        context.contextType() == ContextType.PLACE
        context.contextId() == 12345L
        context.contextName() == "강남 스터디룸 A"
        context.metadata().isEmpty()
    }

    def "forPlace()에 메타데이터를 포함하여 생성할 수 있다"() {
        given:
        def metadata = [category: "스터디룸", floor: 3]

        when:
        def context = ChatRoomContext.forPlace(12345L, "강남 스터디룸 A", metadata)

        then:
        context.contextType() == ContextType.PLACE
        context.contextId() == 12345L
        context.metadata().size() == 2
        context.metadata()["category"] == "스터디룸"
    }

    def "forOrder()로 주문 컨텍스트를 생성할 수 있다"() {
        when:
        def context = ChatRoomContext.forOrder(999L, "주문 #999")

        then:
        context.contextType() == ContextType.ORDER
        context.contextId() == 999L
        context.contextName() == "주문 #999"
    }

    def "forBooking()으로 예약 컨텍스트를 생성할 수 있다"() {
        when:
        def context = ChatRoomContext.forBooking(888L, "예약 #888")

        then:
        context.contextType() == ContextType.BOOKING
        context.contextId() == 888L
        context.contextName() == "예약 #888"
    }

    def "isPlaceContext()는 PLACE 타입일 때만 true를 반환한다"() {
        expect:
        ChatRoomContext.forPlace(1L, "테스트").isPlaceContext() == true
        ChatRoomContext.forOrder(1L, "테스트").isPlaceContext() == false
        ChatRoomContext.forBooking(1L, "테스트").isPlaceContext() == false
    }

    def "contextType이 null이면 예외가 발생한다"() {
        when:
        new ChatRoomContext(null, 1L, "테스트", [:])

        then:
        thrown(NullPointerException)
    }

    def "contextId가 null이면 예외가 발생한다"() {
        when:
        new ChatRoomContext(ContextType.PLACE, null, "테스트", [:])

        then:
        thrown(NullPointerException)
    }

    def "contextId가 0 이하이면 예외가 발생한다"() {
        when:
        new ChatRoomContext(ContextType.PLACE, 0L, "테스트", [:])

        then:
        thrown(IllegalArgumentException)
    }

    def "metadata가 null이면 빈 맵으로 초기화된다"() {
        when:
        def context = new ChatRoomContext(ContextType.PLACE, 1L, "테스트", null)

        then:
        context.metadata() != null
        context.metadata().isEmpty()
    }

    def "metadata는 불변이다"() {
        given:
        def context = ChatRoomContext.forPlace(1L, "테스트", [key: "value"])

        when:
        context.metadata().put("newKey", "newValue")

        then:
        thrown(UnsupportedOperationException)
    }
}
