package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.GetMessagesQuery
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher
import com.teambind.co.kr.chatdding.application.port.out.UnreadCountCachePort
import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.event.MessageReadEvent
import com.teambind.co.kr.chatdding.domain.message.Message
import com.teambind.co.kr.chatdding.domain.message.MessageId
import com.teambind.co.kr.chatdding.domain.message.MessageRepository
import spock.lang.Specification
import spock.lang.Subject

class GetMessagesServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    MessageRepository messageRepository = Mock()
    UnreadCountCachePort unreadCountCachePort = Mock()
    EventPublisher eventPublisher = Mock()

    @Subject
    GetMessagesService getMessagesService = new GetMessagesService(
            chatRoomRepository,
            messageRepository,
            unreadCountCachePort,
            eventPublisher
    )

    def roomId = RoomId.of(1L)
    def userId = UserId.of(100L)
    def recipientId = UserId.of(200L)

    def "메시지 목록을 조회할 수 있다"() {
        given:
        def query = new GetMessagesQuery(roomId, userId, null, 20)
        unreadCountCachePort.getUnreadCount(_, _) >> Optional.of(0)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)
        def messages = [
                Message.create(MessageId.of(1L), roomId, userId, "메시지1"),
                Message.create(MessageId.of(2L), roomId, recipientId, "메시지2")
        ]

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 20, 0) >> messages

        when:
        def result = getMessagesService.execute(query)

        then:
        result.messages().size() == 2
    }

    def "커서 기반 페이지네이션으로 메시지를 조회할 수 있다"() {
        given:
        def cursorId = MessageId.of(10L)
        def query = new GetMessagesQuery(roomId, userId, cursorId, 20)
        unreadCountCachePort.getUnreadCount(_, _) >> Optional.of(0)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)
        def messages = [
                Message.create(MessageId.of(9L), roomId, userId, "이전 메시지")
        ]

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdBeforeCursor(roomId, cursorId, 20) >> messages

        when:
        def result = getMessagesService.execute(query)

        then:
        result.messages().size() == 1
    }

    def "빈 메시지 목록을 조회할 수 있다"() {
        given:
        def query = new GetMessagesQuery(roomId, userId, null, 20)
        unreadCountCachePort.getUnreadCount(_, _) >> Optional.of(0)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 20, 0) >> []

        when:
        def result = getMessagesService.execute(query)

        then:
        result.messages().isEmpty()
        result.hasMore() == false
    }

    def "삭제된 메시지는 마스킹되어 표시된다"() {
        given:
        def query = new GetMessagesQuery(roomId, userId, null, 20)
        unreadCountCachePort.getUnreadCount(_, _) >> Optional.of(0)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        def visibleMessage = Message.create(MessageId.of(1L), roomId, userId, "보이는 메시지")
        def deletedMessage = Message.create(MessageId.of(2L), roomId, recipientId, "삭제된 메시지")
        deletedMessage.deleteFor(userId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 20, 0) >> [visibleMessage, deletedMessage]

        when:
        def result = getMessagesService.execute(query)

        then:
        result.messages().size() == 2
        result.messages()[0].messageId() == "1"
        result.messages()[0].content() == "보이는 메시지"
        result.messages()[0].deleted() == false
        result.messages()[1].messageId() == "2"
        result.messages()[1].content() == "삭제된 메시지입니다"
        result.messages()[1].deleted() == true
    }

    def "존재하지 않는 채팅방이면 예외가 발생한다"() {
        given:
        def query = new GetMessagesQuery(roomId, userId, null, 20)
        chatRoomRepository.findById(roomId) >> Optional.empty()

        when:
        getMessagesService.execute(query)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.CHAT_ROOM_NOT_FOUND
    }

    def "참여하지 않은 사용자는 접근이 거부된다"() {
        given:
        def nonParticipant = UserId.of(999L)
        def query = new GetMessagesQuery(roomId, nonParticipant, null, 20)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)

        when:
        getMessagesService.execute(query)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.CHAT_ROOM_ACCESS_DENIED
    }

    def "limit 개수만큼 조회하고 hasMore를 계산한다"() {
        given:
        def query = new GetMessagesQuery(roomId, userId, null, 2)
        unreadCountCachePort.getUnreadCount(_, _) >> Optional.of(0)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)
        def messages = [
                Message.create(MessageId.of(1L), roomId, userId, "메시지1"),
                Message.create(MessageId.of(2L), roomId, recipientId, "메시지2"),
                Message.create(MessageId.of(3L), roomId, userId, "메시지3")
        ]

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 2, 0) >> messages.take(2)

        when:
        def result = getMessagesService.execute(query)

        then:
        result.messages().size() == 2
    }

    def "커서가 없으면 findByRoomIdOrderByCreatedAtDesc를 호출한다"() {
        given:
        def query = new GetMessagesQuery(roomId, userId, null, 20)
        unreadCountCachePort.getUnreadCount(_, _) >> Optional.of(0)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)

        when:
        getMessagesService.execute(query)

        then:
        1 * messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 20, 0) >> []
        0 * messageRepository.findByRoomIdBeforeCursor(_, _, _)
    }

    def "커서가 있으면 findByRoomIdBeforeCursor를 호출한다"() {
        given:
        def cursorId = MessageId.of(10L)
        def query = new GetMessagesQuery(roomId, userId, cursorId, 20)
        unreadCountCachePort.getUnreadCount(_, _) >> Optional.of(0)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)

        when:
        getMessagesService.execute(query)

        then:
        0 * messageRepository.findByRoomIdOrderByCreatedAtDesc(_, _, _)
        1 * messageRepository.findByRoomIdBeforeCursor(roomId, cursorId, 20) >> []
    }

    def "메시지 조회 시 unreadCount가 0이 아니면 자동 읽음 처리가 트리거된다"() {
        given:
        def testRoomId = RoomId.of(999L)
        def testUserId = UserId.of(888L)
        def query = new GetMessagesQuery(testRoomId, testUserId, null, 20)
        def chatRoom = ChatRoom.createDm(testRoomId, testUserId, recipientId)

        chatRoomRepository.findById(testRoomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(testRoomId, 20, 0) >> []
        unreadCountCachePort.getUnreadCount(testRoomId, testUserId) >> Optional.of(5)

        when:
        getMessagesService.execute(query)

        then:
        1 * unreadCountCachePort.resetUnreadCount(testRoomId, testUserId)
        1 * eventPublisher.publish(_ as MessageReadEvent)
    }

    def "메시지 조회 시 unreadCount가 0이면 읽음 처리를 스킵한다"() {
        given:
        def query = new GetMessagesQuery(roomId, userId, null, 20)
        unreadCountCachePort.getUnreadCount(_, _) >> Optional.of(0)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 20, 0) >> []

        when:
        getMessagesService.execute(query)

        then:
        0 * unreadCountCachePort.resetUnreadCount(_, _)
        0 * eventPublisher.publish(_)
    }

    def "메시지 조회 시 캐시 미스이면 읽음 처리가 트리거된다"() {
        given:
        def testRoomId = RoomId.of(777L)
        def testUserId = UserId.of(666L)
        def query = new GetMessagesQuery(testRoomId, testUserId, null, 20)
        def chatRoom = ChatRoom.createDm(testRoomId, testUserId, recipientId)

        chatRoomRepository.findById(testRoomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(testRoomId, 20, 0) >> []
        unreadCountCachePort.getUnreadCount(testRoomId, testUserId) >> Optional.empty()

        when:
        getMessagesService.execute(query)

        then:
        1 * unreadCountCachePort.resetUnreadCount(testRoomId, testUserId)
        1 * eventPublisher.publish(_ as MessageReadEvent)
    }
}
