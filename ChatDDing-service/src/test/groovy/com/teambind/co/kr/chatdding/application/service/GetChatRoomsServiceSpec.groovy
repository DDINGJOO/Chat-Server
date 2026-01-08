package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.GetChatRoomsQuery
import com.teambind.co.kr.chatdding.application.port.out.UnreadCountCachePort
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.message.Message
import com.teambind.co.kr.chatdding.domain.message.MessageId
import com.teambind.co.kr.chatdding.domain.message.MessageRepository
import spock.lang.Specification
import spock.lang.Subject

class GetChatRoomsServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    MessageRepository messageRepository = Mock()
    UnreadCountCachePort unreadCountCachePort = Mock()

    @Subject
    GetChatRoomsService getChatRoomsService = new GetChatRoomsService(
            chatRoomRepository,
            messageRepository,
            unreadCountCachePort
    )

    def userId = UserId.of(100L)

    def setup() {
        unreadCountCachePort.getUnreadCounts(_, _) >> [:]
    }

    def "채팅방 목록을 조회할 수 있다"() {
        given:
        def query = GetChatRoomsQuery.of(userId.getValue())
        def chatRoom1 = ChatRoom.createDm(RoomId.of(1L), userId, UserId.of(200L))
        def chatRoom2 = ChatRoom.createDm(RoomId.of(2L), userId, UserId.of(300L))

        chatRoomRepository.findActiveByParticipantUserIdOrderByLastMessageAtDesc(userId) >> [chatRoom1, chatRoom2]
        messageRepository.findLatestByRoomId(_) >> Optional.empty()
        messageRepository.countUnreadByRoomIdAndUserId(_, userId) >> 0

        when:
        def result = getChatRoomsService.execute(query)

        then:
        result.chatRooms().size() == 2
    }

    def "빈 채팅방 목록을 조회할 수 있다"() {
        given:
        def query = GetChatRoomsQuery.of(userId.getValue())
        chatRoomRepository.findActiveByParticipantUserIdOrderByLastMessageAtDesc(userId) >> []

        when:
        def result = getChatRoomsService.execute(query)

        then:
        result.chatRooms().isEmpty()
    }

    def "채팅방 목록에 마지막 메시지 정보가 포함된다"() {
        given:
        def query = GetChatRoomsQuery.of(userId.getValue())
        def roomId = RoomId.of(1L)
        def chatRoom = ChatRoom.createDm(roomId, userId, UserId.of(200L))
        def lastMessage = Message.create(MessageId.of(1L), roomId, UserId.of(200L), "마지막 메시지")

        chatRoomRepository.findActiveByParticipantUserIdOrderByLastMessageAtDesc(userId) >> [chatRoom]
        messageRepository.findLatestByRoomId(roomId) >> Optional.of(lastMessage)
        messageRepository.countUnreadByRoomIdAndUserId(roomId, userId) >> 0

        when:
        def result = getChatRoomsService.execute(query)

        then:
        result.chatRooms()[0].lastMessage() == "마지막 메시지"
    }

    def "채팅방 목록에 읽지 않은 메시지 수가 포함된다"() {
        given:
        def query = GetChatRoomsQuery.of(userId.getValue())
        def roomId = RoomId.of(1L)
        def chatRoom = ChatRoom.createDm(roomId, userId, UserId.of(200L))

        chatRoomRepository.findActiveByParticipantUserIdOrderByLastMessageAtDesc(userId) >> [chatRoom]
        messageRepository.findLatestByRoomId(roomId) >> Optional.empty()
        messageRepository.countUnreadByRoomIdAndUserId(roomId, userId) >> 5

        when:
        def result = getChatRoomsService.execute(query)

        then:
        result.chatRooms()[0].unreadCount() == 5
    }

    def "여러 채팅방의 unreadCount가 각각 조회된다"() {
        given:
        def query = GetChatRoomsQuery.of(userId.getValue())
        def room1 = ChatRoom.createDm(RoomId.of(1L), userId, UserId.of(200L))
        def room2 = ChatRoom.createDm(RoomId.of(2L), userId, UserId.of(300L))

        chatRoomRepository.findActiveByParticipantUserIdOrderByLastMessageAtDesc(userId) >> [room1, room2]
        messageRepository.findLatestByRoomId(RoomId.of(1L)) >> Optional.empty()
        messageRepository.findLatestByRoomId(RoomId.of(2L)) >> Optional.empty()
        messageRepository.countUnreadByRoomIdAndUserId(RoomId.of(1L), userId) >> 3
        messageRepository.countUnreadByRoomIdAndUserId(RoomId.of(2L), userId) >> 7

        when:
        def result = getChatRoomsService.execute(query)

        then:
        result.chatRooms()[0].unreadCount() == 3
        result.chatRooms()[1].unreadCount() == 7
    }

    def "마지막 메시지가 없으면 null로 표시된다"() {
        given:
        def query = GetChatRoomsQuery.of(userId.getValue())
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), userId, UserId.of(200L))

        chatRoomRepository.findActiveByParticipantUserIdOrderByLastMessageAtDesc(userId) >> [chatRoom]
        messageRepository.findLatestByRoomId(_) >> Optional.empty()
        messageRepository.countUnreadByRoomIdAndUserId(_, userId) >> 0

        when:
        def result = getChatRoomsService.execute(query)

        then:
        result.chatRooms()[0].lastMessage() == null
    }
}
