package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.CloseSupportChatCommand
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher
import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.event.SupportChatClosedEvent
import spock.lang.Specification
import spock.lang.Subject

class CloseSupportChatServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    EventPublisher eventPublisher = Mock()

    @Subject
    CloseSupportChatService service

    def setup() {
        service = new CloseSupportChatService(chatRoomRepository, eventPublisher)
    }

    def "사용자가 상담을 종료할 수 있다"() {
        given:
        def command = CloseSupportChatCommand.of("1", 100L)
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)
        chatRoomRepository.save(_) >> { ChatRoom room -> room }

        when:
        def result = service.execute(command)

        then:
        result.roomId() == "1"
        result.closedAt() != null

        and:
        1 * eventPublisher.publish(_ as SupportChatClosedEvent)
    }

    def "상담원이 상담을 종료할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        chatRoom.assignAgent(UserId.of(999L))
        def command = CloseSupportChatCommand.of("1", 999L)

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)
        chatRoomRepository.save(_) >> { ChatRoom room -> room }

        when:
        def result = service.execute(command)

        then:
        result.roomId() == "1"
        result.closedAt() != null

        and:
        1 * eventPublisher.publish(_ as SupportChatClosedEvent)
    }

    def "존재하지 않는 채팅방이면 예외가 발생한다"() {
        given:
        def command = CloseSupportChatCommand.of("999", 100L)

        chatRoomRepository.findById(RoomId.of(999L)) >> Optional.empty()

        when:
        service.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.ROOM_NOT_FOUND
    }

    def "SUPPORT 타입이 아닌 채팅방이면 예외가 발생한다"() {
        given:
        def command = CloseSupportChatCommand.of("1", 100L)
        def dmRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(100L), UserId.of(200L))

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(dmRoom)

        when:
        service.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.NOT_SUPPORT_ROOM
    }

    def "이미 종료된 채팅방이면 예외가 발생한다"() {
        given:
        def command = CloseSupportChatCommand.of("1", 100L)
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        chatRoom.close()

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)

        when:
        service.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.ROOM_ALREADY_CLOSED
    }

    def "참여자가 아닌 사용자가 종료 시도하면 예외가 발생한다"() {
        given:
        def command = CloseSupportChatCommand.of("1", 888L)
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))

        chatRoomRepository.findById(RoomId.of(1L)) >> Optional.of(chatRoom)

        when:
        service.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.NOT_PARTICIPANT
    }

    def "roomId가 null이면 예외가 발생한다"() {
        when:
        CloseSupportChatCommand.of(null, 100L)

        then:
        thrown(Exception)
    }

    def "userId가 null이면 예외가 발생한다"() {
        when:
        CloseSupportChatCommand.of("1", null)

        then:
        thrown(Exception)
    }
}
