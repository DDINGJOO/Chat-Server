package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.DeleteMessageCommand
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher
import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.event.MessageDeletedEvent
import com.teambind.co.kr.chatdding.domain.message.Message
import com.teambind.co.kr.chatdding.domain.message.MessageId
import com.teambind.co.kr.chatdding.domain.message.MessageRepository
import spock.lang.Specification
import spock.lang.Subject

class DeleteMessageServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    MessageRepository messageRepository = Mock()
    EventPublisher eventPublisher = Mock()

    @Subject
    DeleteMessageService service

    def setup() {
        service = new DeleteMessageService(chatRoomRepository, messageRepository, eventPublisher)
    }

    def "메시지 Soft Delete 성공 (발신자만 삭제)"() {
        given:
        def command = DeleteMessageCommand.of("1", "100", 10L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(10L), UserId.of(20L))
        def message = Message.create(MessageId.of(100L), RoomId.of(1L), UserId.of(10L), "테스트")

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)
        messageRepository.findById(MessageId.of(100L)) >> Optional.of(message)
        messageRepository.save(_) >> { Message m -> m }

        when:
        def result = service.execute(command)

        then:
        result.messageId() == "100"
        result.hardDeleted() == false
        result.deletedAt() != null

        and:
        1 * messageRepository.save(_)
        0 * messageRepository.deleteById(_)
        1 * eventPublisher.publish({ MessageDeletedEvent e ->
            e.hardDeleted() == false
        })
    }

    def "메시지 Hard Delete 성공 (DM에서 양측 모두 삭제)"() {
        given:
        def command = DeleteMessageCommand.of("1", "100", 20L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(10L), UserId.of(20L))
        def message = Message.create(MessageId.of(100L), RoomId.of(1L), UserId.of(10L), "테스트")
        message.deleteFor(UserId.of(10L)) // 발신자가 이미 삭제함

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)
        messageRepository.findById(MessageId.of(100L)) >> Optional.of(message)

        when:
        def result = service.execute(command)

        then:
        result.messageId() == "100"
        result.hardDeleted() == true

        and:
        0 * messageRepository.save(_)
        1 * messageRepository.deleteById(MessageId.of(100L))
        1 * eventPublisher.publish({ MessageDeletedEvent e ->
            e.hardDeleted() == true
        })
    }

    def "존재하지 않는 채팅방이면 예외 발생"() {
        given:
        def command = DeleteMessageCommand.of("999", "100", 10L)

        chatRoomRepository.findById(RoomId.of(999L)) >> Optional.empty()

        when:
        service.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.ROOM_NOT_FOUND
    }

    def "참여자가 아닌 사용자가 삭제 시도하면 예외 발생"() {
        given:
        def command = DeleteMessageCommand.of("1", "100", 999L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(10L), UserId.of(20L))

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)

        when:
        service.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.NOT_PARTICIPANT
    }

    def "존재하지 않는 메시지면 예외 발생"() {
        given:
        def command = DeleteMessageCommand.of("1", "999", 10L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(10L), UserId.of(20L))

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)
        messageRepository.findById(MessageId.of(999L)) >> Optional.empty()

        when:
        service.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.MESSAGE_NOT_FOUND
    }

    def "다른 채팅방의 메시지면 예외 발생"() {
        given:
        def command = DeleteMessageCommand.of("1", "100", 10L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(10L), UserId.of(20L))
        def message = Message.create(MessageId.of(100L), RoomId.of(999L), UserId.of(10L), "테스트")

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)
        messageRepository.findById(MessageId.of(100L)) >> Optional.of(message)

        when:
        service.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.MESSAGE_NOT_FOUND
    }

    def "그룹채팅에서 일부만 삭제 시 Soft Delete"() {
        given:
        def command = DeleteMessageCommand.of("1", "100", 10L)
        def chatRoom = ChatRoom.createGroup(RoomId.of(1L), UserId.of(10L), [UserId.of(20L), UserId.of(30L)], "그룹")
        def message = Message.create(MessageId.of(100L), RoomId.of(1L), UserId.of(10L), "테스트")

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)
        messageRepository.findById(MessageId.of(100L)) >> Optional.of(message)
        messageRepository.save(_) >> { Message m -> m }

        when:
        def result = service.execute(command)

        then:
        result.hardDeleted() == false
        1 * messageRepository.save(_)
        0 * messageRepository.deleteById(_)
    }
}
