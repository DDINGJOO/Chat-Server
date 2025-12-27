package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomDetailQuery
import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.message.MessageRepository
import spock.lang.Specification
import spock.lang.Subject

class GetChatRoomDetailServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    MessageRepository messageRepository = Mock()

    @Subject
    GetChatRoomDetailService getChatRoomDetailService = new GetChatRoomDetailService(
            chatRoomRepository,
            messageRepository
    )

    def roomId = RoomId.of(1L)
    def userId = UserId.of(100L)
    def recipientId = UserId.of(200L)

    def "채팅방 상세 정보를 조회할 수 있다"() {
        given:
        def query = new GetChatRoomDetailQuery(roomId, userId)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.countUnreadByRoomIdAndUserId(roomId, userId) >> 0

        when:
        def result = getChatRoomDetailService.execute(query)

        then:
        result.roomId() == roomId.toStringValue()
        result.participants().size() == 2
    }

    def "채팅방 상세 정보에 unreadCount가 포함된다"() {
        given:
        def query = new GetChatRoomDetailQuery(roomId, userId)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.countUnreadByRoomIdAndUserId(roomId, userId) >> 10

        when:
        def result = getChatRoomDetailService.execute(query)

        then:
        result.unreadCount() == 10
    }

    def "그룹 채팅방 상세 정보를 조회할 수 있다"() {
        given:
        def groupRoomId = RoomId.of(2L)
        def ownerId = UserId.of(1L)
        def chatRoom = ChatRoom.createGroup(groupRoomId, ownerId, [UserId.of(2L), UserId.of(3L)], "테스트 그룹")
        def query = new GetChatRoomDetailQuery(groupRoomId, ownerId)

        chatRoomRepository.findById(groupRoomId) >> Optional.of(chatRoom)
        messageRepository.countUnreadByRoomIdAndUserId(groupRoomId, ownerId) >> 0

        when:
        def result = getChatRoomDetailService.execute(query)

        then:
        result.name() == "테스트 그룹"
        result.participants().size() == 3
        result.ownerId() == ownerId.getValue()
    }

    def "존재하지 않는 채팅방이면 예외가 발생한다"() {
        given:
        def query = new GetChatRoomDetailQuery(roomId, userId)
        chatRoomRepository.findById(roomId) >> Optional.empty()

        when:
        getChatRoomDetailService.execute(query)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.CHAT_ROOM_NOT_FOUND
    }

    def "참여하지 않은 사용자는 접근이 거부된다"() {
        given:
        def nonParticipant = UserId.of(999L)
        def query = new GetChatRoomDetailQuery(roomId, nonParticipant)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)

        when:
        getChatRoomDetailService.execute(query)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.CHAT_ROOM_ACCESS_DENIED
    }

    def "채팅방 상세 정보에 status가 포함된다"() {
        given:
        def query = new GetChatRoomDetailQuery(roomId, userId)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.countUnreadByRoomIdAndUserId(roomId, userId) >> 0

        when:
        def result = getChatRoomDetailService.execute(query)

        then:
        result.status() != null
    }

    def "채팅방 상세 정보에 createdAt이 포함된다"() {
        given:
        def query = new GetChatRoomDetailQuery(roomId, userId)
        def chatRoom = ChatRoom.createDm(roomId, userId, recipientId)

        chatRoomRepository.findById(roomId) >> Optional.of(chatRoom)
        messageRepository.countUnreadByRoomIdAndUserId(roomId, userId) >> 0

        when:
        def result = getChatRoomDetailService.execute(query)

        then:
        result.createdAt() != null
    }
}
