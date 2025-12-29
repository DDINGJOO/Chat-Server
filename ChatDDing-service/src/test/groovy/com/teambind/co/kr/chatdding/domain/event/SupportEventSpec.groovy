package com.teambind.co.kr.chatdding.domain.event

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import spock.lang.Specification

class SupportEventSpec extends Specification {

    // ========================
    // SupportRequestCreatedEvent 테스트
    // ========================

    def "SupportRequestCreatedEvent.from()으로 이벤트를 생성할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        def category = "일반 문의"

        when:
        def event = SupportRequestCreatedEvent.from(chatRoom, category)

        then:
        event.roomId() == "1"
        event.userId() == 100L
        event.category() == "일반 문의"
        event.eventType == SupportRequestCreatedEvent.EVENT_TYPE
        event.occurredAt() != null
    }

    def "SupportRequestCreatedEvent.from()에 category 없이 생성할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))

        when:
        def event = SupportRequestCreatedEvent.from(chatRoom)

        then:
        event.roomId() == "1"
        event.userId() == 100L
        event.category() == null
        event.eventType == "SUPPORT_REQUEST_CREATED"
    }

    def "SupportRequestCreatedEvent의 EVENT_TYPE이 올바르다"() {
        expect:
        SupportRequestCreatedEvent.EVENT_TYPE == "SUPPORT_REQUEST_CREATED"
    }

    // ========================
    // SupportAgentAssignedEvent 테스트
    // ========================

    def "SupportAgentAssignedEvent.from()으로 이벤트를 생성할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        def agentId = UserId.of(999L)

        when:
        def event = SupportAgentAssignedEvent.from(chatRoom, agentId)

        then:
        event.roomId() == "1"
        event.userId() == 100L
        event.agentId() == 999L
        event.eventType == SupportAgentAssignedEvent.EVENT_TYPE
        event.occurredAt() != null
    }

    def "SupportAgentAssignedEvent의 EVENT_TYPE이 올바르다"() {
        expect:
        SupportAgentAssignedEvent.EVENT_TYPE == "SUPPORT_AGENT_ASSIGNED"
    }

    // ========================
    // SupportChatClosedEvent 테스트
    // ========================

    def "SupportChatClosedEvent.from()으로 상담원 없이 이벤트를 생성할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))

        when:
        def event = SupportChatClosedEvent.from(chatRoom)

        then:
        event.roomId() == "1"
        event.userId() == 100L
        event.agentId() == null
        event.eventType == SupportChatClosedEvent.EVENT_TYPE
        event.occurredAt() != null
    }

    def "SupportChatClosedEvent.from()으로 상담원 배정 후 이벤트를 생성할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        chatRoom.assignAgent(UserId.of(999L))

        when:
        def event = SupportChatClosedEvent.from(chatRoom)

        then:
        event.roomId() == "1"
        event.userId() == 100L
        event.agentId() == 999L
        event.eventType == "SUPPORT_CHAT_CLOSED"
    }

    def "SupportChatClosedEvent의 EVENT_TYPE이 올바르다"() {
        expect:
        SupportChatClosedEvent.EVENT_TYPE == "SUPPORT_CHAT_CLOSED"
    }

    // ========================
    // ChatEvent 인터페이스 구현 검증
    // ========================

    def "모든 Support 이벤트는 ChatEvent 인터페이스를 구현한다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))

        expect:
        SupportRequestCreatedEvent.from(chatRoom) instanceof ChatEvent
        SupportAgentAssignedEvent.from(chatRoom, UserId.of(999L)) instanceof ChatEvent
        SupportChatClosedEvent.from(chatRoom) instanceof ChatEvent
    }

    def "모든 Support 이벤트의 getEventType()이 올바른 값을 반환한다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))

        when:
        def requestEvent = SupportRequestCreatedEvent.from(chatRoom)
        def assignEvent = SupportAgentAssignedEvent.from(chatRoom, UserId.of(999L))
        def closeEvent = SupportChatClosedEvent.from(chatRoom)

        then:
        requestEvent.getEventType() == "SUPPORT_REQUEST_CREATED"
        assignEvent.getEventType() == "SUPPORT_AGENT_ASSIGNED"
        closeEvent.getEventType() == "SUPPORT_CHAT_CLOSED"
    }

    def "모든 Support 이벤트의 getOccurredAt()이 null이 아니다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))

        when:
        def requestEvent = SupportRequestCreatedEvent.from(chatRoom)
        def assignEvent = SupportAgentAssignedEvent.from(chatRoom, UserId.of(999L))
        def closeEvent = SupportChatClosedEvent.from(chatRoom)

        then:
        requestEvent.getOccurredAt() != null
        assignEvent.getOccurredAt() != null
        closeEvent.getOccurredAt() != null
    }
}
