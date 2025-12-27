package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.adapter

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.message.Message
import com.teambind.co.kr.chatdding.domain.message.MessageId
import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.repository.MessageMongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Subject

@DataMongoTest
@Import(MessageRepositoryAdapter)
@ActiveProfiles("test")
class MessageRepositoryAdapterSpec extends Specification {

    @Autowired
    MessageMongoRepository mongoRepository

    @Autowired
    MongoTemplate mongoTemplate

    @Autowired
    @Subject
    MessageRepositoryAdapter messageRepositoryAdapter

    def roomId = RoomId.of(1L)
    def senderId = UserId.of(100L)

    def setup() {
        mongoRepository.deleteAll()
    }

    def "메시지를 저장하고 조회할 수 있다"() {
        given:
        def messageId = MessageId.of(1L)
        def message = Message.create(messageId, roomId, senderId, "테스트 메시지")

        when:
        def saved = messageRepositoryAdapter.save(message)
        def found = messageRepositoryAdapter.findById(messageId)

        then:
        saved.id == messageId
        found.isPresent()
        found.get().content == "테스트 메시지"
    }

    def "존재하지 않는 메시지 조회 시 빈 Optional 반환"() {
        when:
        def found = messageRepositoryAdapter.findById(MessageId.of(999L))

        then:
        found.isEmpty()
    }

    def "채팅방의 메시지를 최신순으로 조회할 수 있다"() {
        given:
        (1..5).each { i ->
            def message = Message.create(MessageId.of(i as Long), roomId, senderId, "메시지 $i")
            messageRepositoryAdapter.save(message)
            sleep(10) // 시간 차이를 두기 위해
        }

        when:
        def messages = messageRepositoryAdapter.findByRoomIdOrderByCreatedAtDesc(roomId, 3, 0)

        then:
        messages.size() == 3
        messages[0].id.getValue() == 5L
    }

    def "커서 기반 페이지네이션으로 메시지를 조회할 수 있다"() {
        given:
        (1..10).each { i ->
            def message = Message.create(MessageId.of(i as Long), roomId, senderId, "메시지 $i")
            messageRepositoryAdapter.save(message)
        }

        when:
        def messages = messageRepositoryAdapter.findByRoomIdBeforeCursor(roomId, MessageId.of(8L), 3)

        then:
        messages.size() == 3
        messages.every { it.id.getValue() < 8L }
    }

    def "특정 시간 이후의 메시지를 조회할 수 있다"() {
        given:
        def msg1 = Message.create(MessageId.of(1L), roomId, senderId, "이전 메시지")
        messageRepositoryAdapter.save(msg1)
        sleep(100)

        def baseTime = java.time.LocalDateTime.now()
        sleep(100)

        def msg2 = Message.create(MessageId.of(2L), roomId, senderId, "이후 메시지")
        messageRepositoryAdapter.save(msg2)

        when:
        def messages = messageRepositoryAdapter.findByRoomIdAndCreatedAtAfter(roomId, baseTime)

        then:
        messages.size() >= 1
        messages.any { it.content == "이후 메시지" }
    }

    def "읽지 않은 메시지 수를 계산할 수 있다"() {
        given:
        def reader = UserId.of(200L)

        // 읽지 않은 메시지 3개
        (1..3).each { i ->
            def message = Message.create(MessageId.of(i as Long), roomId, senderId, "메시지 $i")
            messageRepositoryAdapter.save(message)
        }

        // 읽은 메시지 1개
        def readMessage = Message.create(MessageId.of(4L), roomId, senderId, "읽은 메시지")
        readMessage.markAsRead(reader)
        messageRepositoryAdapter.save(readMessage)

        when:
        def count = messageRepositoryAdapter.countUnreadByRoomIdAndUserId(roomId, reader)

        then:
        count == 3
    }

    def "최신 메시지를 조회할 수 있다"() {
        given:
        (1..5).each { i ->
            def message = Message.create(MessageId.of(i as Long), roomId, senderId, "메시지 $i")
            messageRepositoryAdapter.save(message)
            sleep(10)
        }

        when:
        def latest = messageRepositoryAdapter.findLatestByRoomId(roomId)

        then:
        latest.isPresent()
        latest.get().id.getValue() == 5L
    }

    def "메시지가 없을 때 최신 메시지 조회 시 빈 Optional 반환"() {
        when:
        def latest = messageRepositoryAdapter.findLatestByRoomId(roomId)

        then:
        latest.isEmpty()
    }

    def "메시지를 삭제할 수 있다"() {
        given:
        def messageId = MessageId.of(1L)
        def message = Message.create(messageId, roomId, senderId, "삭제할 메시지")
        messageRepositoryAdapter.save(message)

        when:
        messageRepositoryAdapter.deleteById(messageId)

        then:
        messageRepositoryAdapter.findById(messageId).isEmpty()
    }

    def "채팅방의 모든 메시지를 삭제할 수 있다"() {
        given:
        (1..5).each { i ->
            def message = Message.create(MessageId.of(i as Long), roomId, senderId, "메시지 $i")
            messageRepositoryAdapter.save(message)
        }

        when:
        messageRepositoryAdapter.deleteAllByRoomId(roomId)

        then:
        messageRepositoryAdapter.findByRoomIdOrderByCreatedAtDesc(roomId, 10, 0).isEmpty()
    }

    def "읽음 상태가 올바르게 저장된다"() {
        given:
        def messageId = MessageId.of(1L)
        def message = Message.create(messageId, roomId, senderId, "테스트")
        def reader = UserId.of(200L)

        when:
        message.markAsRead(reader)
        messageRepositoryAdapter.save(message)
        def found = messageRepositoryAdapter.findById(messageId)

        then:
        found.isPresent()
        found.get().isReadBy(reader) == true
        found.get().isReadBy(senderId) == true
    }

    def "다른 채팅방의 메시지는 조회되지 않는다"() {
        given:
        def otherRoomId = RoomId.of(999L)

        def msg1 = Message.create(MessageId.of(1L), roomId, senderId, "채팅방1 메시지")
        def msg2 = Message.create(MessageId.of(2L), otherRoomId, senderId, "채팅방2 메시지")

        messageRepositoryAdapter.save(msg1)
        messageRepositoryAdapter.save(msg2)

        when:
        def messages = messageRepositoryAdapter.findByRoomIdOrderByCreatedAtDesc(roomId, 10, 0)

        then:
        messages.size() == 1
        messages[0].roomId == roomId
    }
}
