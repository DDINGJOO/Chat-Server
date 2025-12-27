package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.SendMessageCommand
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher
import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.common.util.generator.PrimaryKeyGenerator
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.event.MessageSentEvent
import com.teambind.co.kr.chatdding.domain.message.Message
import com.teambind.co.kr.chatdding.domain.message.MessageRepository
import spock.lang.Specification
import spock.lang.Subject

class SendMessageServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    MessageRepository messageRepository = Mock()
    PrimaryKeyGenerator primaryKeyGenerator = Mock()
    EventPublisher eventPublisher = Mock()

    @Subject
    SendMessageService sendMessageService = new SendMessageService(
            chatRoomRepository,
            messageRepository,
            primaryKeyGenerator,
            eventPublisher
    )

    def roomId = RoomId.of(1L)
    def senderId = UserId.of(100L)
    def recipientId = UserId.of(200L)

    def "메시지 전송이 성공하면 결과를 반환한다"() {
        given:
        def command = new SendMessageCommand(roomId, senderId, "안녕하세요!")
        def chatRoom = ChatRoom.createDm(roomId, senderId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        primaryKeyGenerator.generateLongKey() >> 999L
        messageRepository.save(_) >> { Message msg -> msg }

        when:
        def result = sendMessageService.execute(command)

        then:
        result != null
        result.roomId() == roomId.toStringValue()
        result.senderId() == senderId.getValue()
        result.content() == "안녕하세요!"
    }

    def "메시지 전송 시 채팅방을 저장하고 이벤트를 발행한다"() {
        given:
        def command = new SendMessageCommand(roomId, senderId, "테스트 메시지")
        def chatRoom = ChatRoom.createDm(roomId, senderId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        primaryKeyGenerator.generateLongKey() >> 999L
        messageRepository.save(_) >> { Message msg -> msg }

        when:
        sendMessageService.execute(command)

        then:
        1 * chatRoomRepository.save(_)
        1 * eventPublisher.publish(_ as MessageSentEvent)
    }

    def "이벤트에 수신자 목록이 포함된다 (발신자 제외)"() {
        given:
        def command = new SendMessageCommand(roomId, senderId, "테스트")
        def chatRoom = ChatRoom.createDm(roomId, senderId, recipientId)
        MessageSentEvent capturedEvent = null

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        primaryKeyGenerator.generateLongKey() >> 999L
        messageRepository.save(_) >> { Message msg -> msg }
        eventPublisher.publish(_) >> { MessageSentEvent event -> capturedEvent = event }

        when:
        sendMessageService.execute(command)

        then:
        capturedEvent != null
        capturedEvent.recipientIds() == [recipientId.getValue()]
        !capturedEvent.recipientIds().contains(senderId.getValue())
    }

    def "존재하지 않는 채팅방이면 예외가 발생한다"() {
        given:
        def command = new SendMessageCommand(roomId, senderId, "테스트")
        chatRoomRepository.findById(roomId) >> Optional.empty()

        when:
        sendMessageService.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.CHAT_ROOM_NOT_FOUND
    }

    def "참여하지 않은 사용자가 메시지를 보내면 예외가 발생한다"() {
        given:
        def nonParticipant = UserId.of(999L)
        def command = new SendMessageCommand(roomId, nonParticipant, "테스트")
        def chatRoom = ChatRoom.createDm(roomId, senderId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)

        when:
        sendMessageService.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.CHAT_ROOM_ACCESS_DENIED
    }

    def "종료된 채팅방에서 메시지를 보내면 예외가 발생한다"() {
        given:
        def command = new SendMessageCommand(roomId, senderId, "테스트")
        def chatRoom = ChatRoom.createDm(roomId, senderId, recipientId)
        chatRoom.close()

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)

        when:
        sendMessageService.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.CHAT_ROOM_ACCESS_DENIED
    }

    def "메시지 전송 후 채팅방의 lastMessageAt이 업데이트된다"() {
        given:
        def command = new SendMessageCommand(roomId, senderId, "테스트")
        def chatRoom = ChatRoom.createDm(roomId, senderId, recipientId)
        def originalLastMessageAt = chatRoom.lastMessageAt
        ChatRoom savedChatRoom = null

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        primaryKeyGenerator.generateLongKey() >> 999L
        messageRepository.save(_) >> { Message msg -> msg }
        chatRoomRepository.save(_) >> { ChatRoom room -> savedChatRoom = room; room }

        when:
        sendMessageService.execute(command)

        then:
        savedChatRoom.lastMessageAt != null
    }

    def "그룹 채팅방에서 모든 다른 참여자에게 이벤트가 전달된다"() {
        given:
        def groupRoomId = RoomId.of(2L)
        def ownerId = UserId.of(1L)
        def member1 = UserId.of(2L)
        def member2 = UserId.of(3L)
        def chatRoom = ChatRoom.createGroup(groupRoomId, ownerId, [member1, member2], "그룹")
        def command = new SendMessageCommand(groupRoomId, ownerId, "그룹 메시지")
        MessageSentEvent capturedEvent = null

        chatRoomRepository.findById(groupRoomId) >> Optional.of(chatRoom)
        primaryKeyGenerator.generateLongKey() >> 999L
        messageRepository.save(_) >> { Message msg -> msg }
        eventPublisher.publish(_) >> { MessageSentEvent event -> capturedEvent = event }

        when:
        sendMessageService.execute(command)

        then:
        capturedEvent.recipientIds().size() == 2
        capturedEvent.recipientIds().containsAll([member1.getValue(), member2.getValue()])
        !capturedEvent.recipientIds().contains(ownerId.getValue())
    }
}
