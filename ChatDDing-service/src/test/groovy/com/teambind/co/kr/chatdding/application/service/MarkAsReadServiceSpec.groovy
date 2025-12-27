package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.MarkAsReadCommand
import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.message.Message
import com.teambind.co.kr.chatdding.domain.message.MessageId
import com.teambind.co.kr.chatdding.domain.message.MessageRepository
import spock.lang.Specification
import spock.lang.Subject

class MarkAsReadServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    MessageRepository messageRepository = Mock()

    @Subject
    MarkAsReadService markAsReadService = new MarkAsReadService(
            chatRoomRepository,
            messageRepository
    )

    def roomId = RoomId.of(1L)
    def userId = UserId.of(100L)
    def senderId = UserId.of(200L)

    def "읽음 처리가 성공하면 결과를 반환한다"() {
        given:
        def command = new MarkAsReadCommand(roomId, userId, null)
        def chatRoom = ChatRoom.createDm(roomId, userId, senderId)
        def unreadMessage = Message.create(MessageId.of(1L), roomId, senderId, "안읽은 메시지")

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 100, 0) >> [unreadMessage]
        messageRepository.save(_) >> { Message msg -> msg }

        when:
        def result = markAsReadService.execute(command)

        then:
        result.roomId() == roomId.toStringValue()
        result.userId() == userId.getValue()
        result.readCount() == 1
        result.readAt() != null
    }

    def "이미 읽은 메시지는 count에 포함되지 않는다"() {
        given:
        def command = new MarkAsReadCommand(roomId, userId, null)
        def chatRoom = ChatRoom.createDm(roomId, userId, senderId)
        def alreadyReadMessage = Message.create(MessageId.of(1L), roomId, senderId, "이미 읽은 메시지")
        alreadyReadMessage.markAsRead(userId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 100, 0) >> [alreadyReadMessage]

        when:
        def result = markAsReadService.execute(command)

        then:
        result.readCount() == 0
        0 * messageRepository.save(_)
    }

    def "여러 메시지를 한번에 읽음 처리할 수 있다"() {
        given:
        def command = new MarkAsReadCommand(roomId, userId, null)
        def chatRoom = ChatRoom.createDm(roomId, userId, senderId)
        def messages = [
                Message.create(MessageId.of(1L), roomId, senderId, "메시지1"),
                Message.create(MessageId.of(2L), roomId, senderId, "메시지2"),
                Message.create(MessageId.of(3L), roomId, senderId, "메시지3")
        ]

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 100, 0) >> messages
        messageRepository.save(_) >> { Message msg -> msg }

        when:
        def result = markAsReadService.execute(command)

        then:
        result.readCount() == 3
        3 * messageRepository.save(_)
    }

    def "lastMessageId가 지정되면 해당 메시지 이후의 메시지만 조회한다"() {
        given:
        def lastMessageId = MessageId.of(5L)
        def command = new MarkAsReadCommand(roomId, userId, lastMessageId)
        def chatRoom = ChatRoom.createDm(roomId, userId, senderId)
        def lastMessage = Message.create(lastMessageId, roomId, senderId, "마지막 메시지")
        def newerMessage = Message.create(MessageId.of(6L), roomId, senderId, "새 메시지")

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findById(lastMessageId) >> Optional.of(lastMessage)
        messageRepository.save(_) >> { Message msg -> msg }

        when:
        markAsReadService.execute(command)

        then:
        1 * messageRepository.findByRoomIdAndCreatedAtAfter(roomId, _) >> [newerMessage]
    }

    def "참여자의 lastReadAt이 업데이트된다"() {
        given:
        def command = new MarkAsReadCommand(roomId, userId, null)
        def chatRoom = ChatRoom.createDm(roomId, userId, senderId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 100, 0) >> []

        when:
        markAsReadService.execute(command)

        then:
        1 * chatRoomRepository.save(_)
    }

    def "존재하지 않는 채팅방이면 예외가 발생한다"() {
        given:
        def command = new MarkAsReadCommand(roomId, userId, null)
        chatRoomRepository.findById(roomId) >> Optional.empty()

        when:
        markAsReadService.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.CHAT_ROOM_NOT_FOUND
    }

    def "참여하지 않은 사용자는 접근이 거부된다"() {
        given:
        def nonParticipant = UserId.of(999L)
        def command = new MarkAsReadCommand(roomId, nonParticipant, null)
        def chatRoom = ChatRoom.createDm(roomId, userId, senderId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)

        when:
        markAsReadService.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.CHAT_ROOM_ACCESS_DENIED
    }

    def "lastMessageId가 존재하지 않으면 예외가 발생한다"() {
        given:
        def nonExistentMessageId = MessageId.of(999L)
        def command = new MarkAsReadCommand(roomId, userId, nonExistentMessageId)
        def chatRoom = ChatRoom.createDm(roomId, userId, senderId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findById(nonExistentMessageId) >> Optional.empty()

        when:
        markAsReadService.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.MESSAGE_NOT_FOUND
    }

    def "읽음 처리 시 메시지의 readBy가 업데이트된다"() {
        given:
        def command = new MarkAsReadCommand(roomId, userId, null)
        def chatRoom = ChatRoom.createDm(roomId, userId, senderId)
        def message = Message.create(MessageId.of(1L), roomId, senderId, "메시지")
        Message savedMessage = null

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, 100, 0) >> [message]
        messageRepository.save(_) >> { Message msg -> savedMessage = msg; msg }

        when:
        markAsReadService.execute(command)

        then:
        savedMessage.isReadBy(userId) == true
    }
}
