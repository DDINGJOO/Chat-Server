package com.teambind.co.kr.chatdding.domain.message

import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

class MessageSpec extends Specification {

    def messageId = MessageId.of(1L)
    def roomId = RoomId.of(100L)
    def senderId = UserId.of(1000L)

    // ========================
    // 메시지 생성 테스트
    // ========================

    def "Message.create()로 메시지를 생성할 수 있다"() {
        given:
        def content = "안녕하세요!"

        when:
        def message = Message.create(messageId, roomId, senderId, content)

        then:
        message.id == messageId
        message.roomId == roomId
        message.senderId == senderId
        message.content == content
        message.createdAt != null
    }

    def "메시지 생성 시 발신자는 자동으로 읽음 처리된다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트 메시지")

        expect:
        message.isReadBy(senderId) == true
        message.readCount == 1
    }

    def "메시지 생성 시 content가 null이면 예외가 발생한다"() {
        when:
        Message.create(messageId, roomId, senderId, null)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.MESSAGE_CONTENT_EMPTY
    }

    @Unroll
    def "메시지 생성 시 content가 빈 문자열('#content')이면 예외가 발생한다"() {
        when:
        Message.create(messageId, roomId, senderId, content)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.MESSAGE_CONTENT_EMPTY

        where:
        content << ["", "   ", "\t", "\n"]
    }

    def "메시지 content가 5000자를 초과하면 예외가 발생한다"() {
        given:
        def longContent = "a" * 5001

        when:
        Message.create(messageId, roomId, senderId, longContent)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.MESSAGE_CONTENT_TOO_LONG
    }

    def "메시지 content가 정확히 5000자이면 성공한다"() {
        given:
        def maxContent = "a" * 5000

        when:
        def message = Message.create(messageId, roomId, senderId, maxContent)

        then:
        message.content.length() == 5000
    }

    // ========================
    // 읽음 처리 테스트
    // ========================

    def "markAsRead()로 메시지를 읽음 처리할 수 있다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트")
        def reader = UserId.of(2000L)

        when:
        message.markAsRead(reader)

        then:
        message.isReadBy(reader) == true
        message.readCount == 2
    }

    def "동일한 사용자가 중복 읽음 처리해도 count는 증가하지 않는다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트")
        def reader = UserId.of(2000L)
        message.markAsRead(reader)
        def originalCount = message.readCount

        when:
        message.markAsRead(reader)

        then:
        message.readCount == originalCount
    }

    def "markAsReadAt()으로 특정 시간에 읽음 처리할 수 있다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트")
        def reader = UserId.of(2000L)
        def readAt = LocalDateTime.of(2024, 6, 1, 12, 0)

        when:
        message.markAsReadAt(reader, readAt)

        then:
        message.isReadBy(reader) == true
        message.readBy.get(reader) == readAt
    }

    def "isReadBy()는 읽지 않은 사용자에 대해 false를 반환한다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트")
        def nonReader = UserId.of(9999L)

        expect:
        message.isReadBy(nonReader) == false
    }

    // ========================
    // 삭제 처리 테스트
    // ========================

    def "deleteFor()로 사용자별 메시지를 삭제할 수 있다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트")
        def deleter = UserId.of(2000L)

        when:
        message.deleteFor(deleter)

        then:
        message.isDeletedFor(deleter) == true
        message.isVisibleTo(deleter) == false
    }

    def "삭제하지 않은 사용자에게는 메시지가 보인다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트")
        def otherUser = UserId.of(2000L)

        expect:
        message.isDeletedFor(otherUser) == false
        message.isVisibleTo(otherUser) == true
    }

    def "여러 사용자가 각각 삭제할 수 있다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트")
        def user1 = UserId.of(2000L)
        def user2 = UserId.of(3000L)

        when:
        message.deleteFor(user1)
        message.deleteFor(user2)

        then:
        message.deletedBy.size() == 2
        message.isDeletedFor(user1) == true
        message.isDeletedFor(user2) == true
    }

    // ========================
    // 미리보기 테스트
    // ========================

    def "getContentPreview()는 50자 이하 메시지를 그대로 반환한다"() {
        given:
        def shortContent = "짧은 메시지"
        def message = Message.create(messageId, roomId, senderId, shortContent)

        expect:
        message.contentPreview == shortContent
    }

    def "getContentPreview()는 50자 초과 메시지를 축약한다"() {
        given:
        def longContent = "a" * 100
        def message = Message.create(messageId, roomId, senderId, longContent)

        expect:
        message.contentPreview == "a" * 50 + "..."
        message.contentPreview.length() == 53
    }

    def "getContentPreview()는 정확히 50자인 메시지를 그대로 반환한다"() {
        given:
        def exactContent = "a" * 50
        def message = Message.create(messageId, roomId, senderId, exactContent)

        expect:
        message.contentPreview == exactContent
    }

    // ========================
    // restore 테스트
    // ========================

    def "restore()로 기존 데이터를 복원할 수 있다"() {
        given:
        def readBy = [(senderId): LocalDateTime.now(), (UserId.of(2000L)): LocalDateTime.now()]
        def deletedBy = [UserId.of(3000L)] as Set
        def createdAt = LocalDateTime.of(2024, 1, 1, 10, 0)

        when:
        def message = Message.restore(messageId, roomId, senderId, "복원된 메시지", readBy, deletedBy, createdAt)

        then:
        message.id == messageId
        message.roomId == roomId
        message.senderId == senderId
        message.content == "복원된 메시지"
        message.readCount == 2
        message.createdAt == createdAt
        message.isDeletedFor(UserId.of(3000L)) == true
    }

    // ========================
    // 불변성 테스트
    // ========================

    def "readBy 맵은 불변이다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트")

        when:
        message.readBy.put(UserId.of(9999L), LocalDateTime.now())

        then:
        thrown(UnsupportedOperationException)
    }

    def "deletedBy 집합은 불변이다"() {
        given:
        def message = Message.create(messageId, roomId, senderId, "테스트")

        when:
        message.deletedBy.add(UserId.of(9999L))

        then:
        thrown(UnsupportedOperationException)
    }
}
