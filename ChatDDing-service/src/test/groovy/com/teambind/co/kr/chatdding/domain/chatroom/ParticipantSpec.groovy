package com.teambind.co.kr.chatdding.domain.chatroom

import com.teambind.co.kr.chatdding.domain.common.UserId
import spock.lang.Specification

import java.time.LocalDateTime

class ParticipantSpec extends Specification {

    def "Participant.create()로 참여자를 생성하면 기본값이 설정된다"() {
        given:
        def userId = UserId.of(1L)

        when:
        def participant = Participant.create(userId)

        then:
        participant.userId == userId
        participant.notificationEnabled == true
        participant.lastReadAt != null
        participant.joinedAt != null
    }

    def "Participant.of()로 모든 값을 지정하여 참여자를 생성할 수 있다"() {
        given:
        def userId = UserId.of(1L)
        def lastReadAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        def joinedAt = LocalDateTime.of(2024, 1, 1, 10, 0)

        when:
        def participant = Participant.of(userId, false, lastReadAt, joinedAt)

        then:
        participant.userId == userId
        participant.notificationEnabled == false
        participant.lastReadAt == lastReadAt
        participant.joinedAt == joinedAt
    }

    def "updateLastReadAt()으로 읽음 시간을 업데이트할 수 있다"() {
        given:
        def participant = Participant.create(UserId.of(1L))
        def originalReadAt = participant.lastReadAt
        def newReadAt = originalReadAt.plusMinutes(10)

        when:
        participant.updateLastReadAt(newReadAt)

        then:
        participant.lastReadAt == newReadAt
    }

    def "updateLastReadAt()은 null을 무시한다"() {
        given:
        def participant = Participant.create(UserId.of(1L))
        def originalReadAt = participant.lastReadAt

        when:
        participant.updateLastReadAt(null)

        then:
        participant.lastReadAt == originalReadAt
    }

    def "updateLastReadAt()은 이전 시간보다 과거인 경우 무시한다"() {
        given:
        def now = LocalDateTime.now()
        def participant = Participant.of(UserId.of(1L), true, now, now.minusHours(1))
        def pastTime = now.minusMinutes(30)

        when:
        participant.updateLastReadAt(pastTime)

        then:
        participant.lastReadAt == now
    }

    def "enableNotification()으로 알림을 활성화할 수 있다"() {
        given:
        def participant = Participant.of(UserId.of(1L), false, LocalDateTime.now(), LocalDateTime.now())

        when:
        participant.enableNotification()

        then:
        participant.notificationEnabled == true
    }

    def "disableNotification()으로 알림을 비활성화할 수 있다"() {
        given:
        def participant = Participant.create(UserId.of(1L))

        when:
        participant.disableNotification()

        then:
        participant.notificationEnabled == false
    }

    def "hasUnreadMessages()는 마지막 메시지 시간이 null이면 false를 반환한다"() {
        given:
        def participant = Participant.create(UserId.of(1L))

        expect:
        participant.hasUnreadMessages(null) == false
    }

    def "hasUnreadMessages()는 lastReadAt이 null이면 true를 반환한다"() {
        given:
        def participant = Participant.of(UserId.of(1L), true, null, LocalDateTime.now())
        def lastMessageAt = LocalDateTime.now()

        expect:
        participant.hasUnreadMessages(lastMessageAt) == true
    }

    def "hasUnreadMessages()는 마지막 메시지가 읽은 시간 이후면 true를 반환한다"() {
        given:
        def lastReadAt = LocalDateTime.now()
        def participant = Participant.of(UserId.of(1L), true, lastReadAt, lastReadAt.minusHours(1))
        def lastMessageAt = lastReadAt.plusMinutes(5)

        expect:
        participant.hasUnreadMessages(lastMessageAt) == true
    }

    def "hasUnreadMessages()는 마지막 메시지가 읽은 시간 이전이면 false를 반환한다"() {
        given:
        def lastReadAt = LocalDateTime.now()
        def participant = Participant.of(UserId.of(1L), true, lastReadAt, lastReadAt.minusHours(1))
        def lastMessageAt = lastReadAt.minusMinutes(5)

        expect:
        participant.hasUnreadMessages(lastMessageAt) == false
    }

    def "hasUnreadMessages()는 마지막 메시지와 읽은 시간이 같으면 false를 반환한다"() {
        given:
        def sameTime = LocalDateTime.now()
        def participant = Participant.of(UserId.of(1L), true, sameTime, sameTime.minusHours(1))

        expect:
        participant.hasUnreadMessages(sameTime) == false
    }
}
