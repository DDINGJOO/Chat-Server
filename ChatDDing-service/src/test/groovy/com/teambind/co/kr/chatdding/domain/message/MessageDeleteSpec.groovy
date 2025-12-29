package com.teambind.co.kr.chatdding.domain.message

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.event.MessageDeletedEvent
import spock.lang.Specification

class MessageDeleteSpec extends Specification {

    def "메시지 삭제 시 deletedBy에 사용자가 추가된다"() {
        given:
        def message = Message.create(
                MessageId.of(1L),
                RoomId.of(100L),
                UserId.of(10L),
                "테스트 메시지"
        )
        def userId = UserId.of(20L)

        when:
        message.deleteFor(userId)

        then:
        message.isDeletedFor(userId)
        message.deletedByCount == 1
    }

    def "동일 사용자 중복 삭제 시 중복되지 않는다"() {
        given:
        def message = Message.create(
                MessageId.of(1L),
                RoomId.of(100L),
                UserId.of(10L),
                "테스트 메시지"
        )
        def userId = UserId.of(20L)

        when:
        message.deleteFor(userId)
        message.deleteFor(userId)

        then:
        message.deletedByCount == 1
    }

    def "DM에서 한 명만 삭제 시 shouldHardDelete는 false"() {
        given:
        def message = Message.create(
                MessageId.of(1L),
                RoomId.of(100L),
                UserId.of(10L),
                "테스트 메시지"
        )
        def participantCount = 2

        when:
        message.deleteFor(UserId.of(10L))

        then:
        !message.shouldHardDelete(participantCount)
    }

    def "DM에서 양측 모두 삭제 시 shouldHardDelete는 true"() {
        given:
        def message = Message.create(
                MessageId.of(1L),
                RoomId.of(100L),
                UserId.of(10L),
                "테스트 메시지"
        )
        def participantCount = 2

        when:
        message.deleteFor(UserId.of(10L))
        message.deleteFor(UserId.of(20L))

        then:
        message.shouldHardDelete(participantCount)
    }

    def "그룹채팅에서 일부만 삭제 시 shouldHardDelete는 false"() {
        given:
        def message = Message.create(
                MessageId.of(1L),
                RoomId.of(100L),
                UserId.of(10L),
                "테스트 메시지"
        )
        def participantCount = 5

        when:
        message.deleteFor(UserId.of(10L))
        message.deleteFor(UserId.of(20L))
        message.deleteFor(UserId.of(30L))

        then:
        !message.shouldHardDelete(participantCount)
    }

    def "그룹채팅에서 모두 삭제 시 shouldHardDelete는 true"() {
        given:
        def message = Message.create(
                MessageId.of(1L),
                RoomId.of(100L),
                UserId.of(10L),
                "테스트 메시지"
        )
        def participantCount = 3

        when:
        message.deleteFor(UserId.of(10L))
        message.deleteFor(UserId.of(20L))
        message.deleteFor(UserId.of(30L))

        then:
        message.shouldHardDelete(participantCount)
    }

    def "삭제되지 않은 사용자에게는 메시지가 보인다"() {
        given:
        def message = Message.create(
                MessageId.of(1L),
                RoomId.of(100L),
                UserId.of(10L),
                "테스트 메시지"
        )
        def user1 = UserId.of(10L)
        def user2 = UserId.of(20L)

        when:
        message.deleteFor(user1)

        then:
        !message.isVisibleTo(user1)
        message.isVisibleTo(user2)
    }

    def "MessageDeletedEvent가 올바르게 생성된다"() {
        when:
        def event = MessageDeletedEvent.of("123", "456", 100L, false)

        then:
        event.messageId() == "123"
        event.roomId() == "456"
        event.deletedBy() == 100L
        event.hardDeleted() == false
        event.eventType == "MESSAGE_DELETED"
        event.occurredAt() != null
    }

    def "Hard Delete 이벤트가 올바르게 생성된다"() {
        when:
        def event = MessageDeletedEvent.of("123", "456", 100L, true)

        then:
        event.hardDeleted() == true
    }
}
