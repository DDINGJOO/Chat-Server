package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.adapter

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.repository.ChatRoomMongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Subject

@DataMongoTest
@Import(ChatRoomRepositoryAdapter)
@ActiveProfiles("test")
class ChatRoomRepositoryAdapterSpec extends Specification {

    @Autowired
    ChatRoomMongoRepository mongoRepository

    @Autowired
    @Subject
    ChatRoomRepositoryAdapter chatRoomRepositoryAdapter

    def setup() {
        mongoRepository.deleteAll()
    }

    def "채팅방을 저장하고 조회할 수 있다"() {
        given:
        def roomId = RoomId.of(1L)
        def chatRoom = ChatRoom.createDm(roomId, UserId.of(100L), UserId.of(200L))

        when:
        def saved = chatRoomRepositoryAdapter.save(chatRoom)
        def found = chatRoomRepositoryAdapter.findById(roomId)

        then:
        saved.id == roomId
        found.isPresent()
        found.get().id == roomId
        found.get().type == ChatRoomType.DM
    }

    def "존재하지 않는 채팅방 조회 시 빈 Optional 반환"() {
        when:
        def found = chatRoomRepositoryAdapter.findById(RoomId.of(999L))

        then:
        found.isEmpty()
    }

    def "참여자 ID로 채팅방 목록을 조회할 수 있다"() {
        given:
        def userId = UserId.of(100L)
        def chatRoom1 = ChatRoom.createDm(RoomId.of(1L), userId, UserId.of(200L))
        def chatRoom2 = ChatRoom.createDm(RoomId.of(2L), userId, UserId.of(300L))
        def chatRoom3 = ChatRoom.createDm(RoomId.of(3L), UserId.of(400L), UserId.of(500L))

        chatRoomRepositoryAdapter.save(chatRoom1)
        chatRoomRepositoryAdapter.save(chatRoom2)
        chatRoomRepositoryAdapter.save(chatRoom3)

        when:
        def rooms = chatRoomRepositoryAdapter.findByParticipantUserId(userId)

        then:
        rooms.size() == 2
    }

    def "활성 채팅방만 lastMessageAt 내림차순으로 조회할 수 있다"() {
        given:
        def userId = UserId.of(100L)
        def activeRoom = ChatRoom.createDm(RoomId.of(1L), userId, UserId.of(200L))
        def closedRoom = ChatRoom.createDm(RoomId.of(2L), userId, UserId.of(300L))
        closedRoom.close()

        chatRoomRepositoryAdapter.save(activeRoom)
        chatRoomRepositoryAdapter.save(closedRoom)

        when:
        def rooms = chatRoomRepositoryAdapter.findActiveByParticipantUserIdOrderByLastMessageAtDesc(userId)

        then:
        rooms.size() == 1
        rooms[0].status == ChatRoomStatus.ACTIVE
    }

    def "DM 채팅방을 참여자 ID로 조회할 수 있다"() {
        given:
        def user1 = UserId.of(100L)
        def user2 = UserId.of(200L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), user1, user2)
        chatRoomRepositoryAdapter.save(chatRoom)

        when:
        def found = chatRoomRepositoryAdapter.findDmByParticipantIds([100L, 200L])

        then:
        found.isPresent()
        found.get().type == ChatRoomType.DM
    }

    def "DM 조회 시 참여자 순서가 달라도 찾을 수 있다"() {
        given:
        def user1 = UserId.of(100L)
        def user2 = UserId.of(200L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), user1, user2)
        chatRoomRepositoryAdapter.save(chatRoom)

        when:
        def found = chatRoomRepositoryAdapter.findDmByParticipantIds([200L, 100L].sort())

        then:
        found.isPresent()
    }

    def "채팅방 존재 여부를 확인할 수 있다"() {
        given:
        def roomId = RoomId.of(1L)
        def chatRoom = ChatRoom.createDm(roomId, UserId.of(100L), UserId.of(200L))
        chatRoomRepositoryAdapter.save(chatRoom)

        expect:
        chatRoomRepositoryAdapter.existsById(roomId) == true
        chatRoomRepositoryAdapter.existsById(RoomId.of(999L)) == false
    }

    def "채팅방을 삭제할 수 있다"() {
        given:
        def roomId = RoomId.of(1L)
        def chatRoom = ChatRoom.createDm(roomId, UserId.of(100L), UserId.of(200L))
        chatRoomRepositoryAdapter.save(chatRoom)

        when:
        chatRoomRepositoryAdapter.deleteById(roomId)

        then:
        chatRoomRepositoryAdapter.findById(roomId).isEmpty()
    }

    def "그룹 채팅방을 저장하고 조회할 수 있다"() {
        given:
        def roomId = RoomId.of(1L)
        def ownerId = UserId.of(100L)
        def chatRoom = ChatRoom.createGroup(roomId, ownerId, [UserId.of(200L), UserId.of(300L)], "테스트 그룹")
        chatRoomRepositoryAdapter.save(chatRoom)

        when:
        def found = chatRoomRepositoryAdapter.findById(roomId)

        then:
        found.isPresent()
        found.get().type == ChatRoomType.GROUP
        found.get().name == "테스트 그룹"
        found.get().participants.size() == 3
    }

    def "채팅방 업데이트가 올바르게 저장된다"() {
        given:
        def roomId = RoomId.of(1L)
        def chatRoom = ChatRoom.createDm(roomId, UserId.of(100L), UserId.of(200L))
        chatRoomRepositoryAdapter.save(chatRoom)

        when:
        chatRoom.close()
        chatRoomRepositoryAdapter.save(chatRoom)
        def found = chatRoomRepositoryAdapter.findById(roomId)

        then:
        found.isPresent()
        found.get().status == ChatRoomStatus.CLOSED
    }

    def "대기 중인 상담 채팅방 목록을 조회할 수 있다"() {
        given:
        def support1 = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        def support2 = ChatRoom.createSupport(RoomId.of(2L), UserId.of(200L))
        def support3 = ChatRoom.createSupport(RoomId.of(3L), UserId.of(300L))

        chatRoomRepositoryAdapter.save(support1)
        chatRoomRepositoryAdapter.save(support2)
        chatRoomRepositoryAdapter.save(support3)

        when:
        def rooms = chatRoomRepositoryAdapter.findPendingSupportRooms(null, 10)

        then:
        rooms.size() == 3
        rooms.every { it.type == ChatRoomType.SUPPORT }
    }

    def "상담원이 배정된 채팅방은 대기열에서 제외된다"() {
        given:
        def pending = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        def assigned = ChatRoom.createSupport(RoomId.of(2L), UserId.of(200L))
        assigned.assignAgent(UserId.of(999L))

        chatRoomRepositoryAdapter.save(pending)
        chatRoomRepositoryAdapter.save(assigned)

        when:
        def rooms = chatRoomRepositoryAdapter.findPendingSupportRooms(null, 10)

        then:
        rooms.size() == 1
        rooms[0].id.value == 1L
    }

    def "종료된 상담 채팅방은 대기열에서 제외된다"() {
        given:
        def pending = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        def closed = ChatRoom.createSupport(RoomId.of(2L), UserId.of(200L))
        closed.close()

        chatRoomRepositoryAdapter.save(pending)
        chatRoomRepositoryAdapter.save(closed)

        when:
        def rooms = chatRoomRepositoryAdapter.findPendingSupportRooms(null, 10)

        then:
        rooms.size() == 1
        rooms[0].id.value == 1L
    }

    def "상담 대기열 커서 기반 페이지네이션이 동작한다"() {
        given:
        (1L..5L).each {
            def room = ChatRoom.createSupport(RoomId.of(it), UserId.of(it * 100))
            chatRoomRepositoryAdapter.save(room)
        }

        when:
        def firstPage = chatRoomRepositoryAdapter.findPendingSupportRooms(null, 2)
        def secondPage = chatRoomRepositoryAdapter.findPendingSupportRooms(firstPage.last().id.value.toString(), 2)

        then:
        firstPage.size() == 2
        secondPage.size() == 2
        secondPage[0].id.value > firstPage.last().id.value
    }

    def "대기 중인 상담 수를 조회할 수 있다"() {
        given:
        def pending1 = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        def pending2 = ChatRoom.createSupport(RoomId.of(2L), UserId.of(200L))
        def assigned = ChatRoom.createSupport(RoomId.of(3L), UserId.of(300L))
        assigned.assignAgent(UserId.of(999L))

        chatRoomRepositoryAdapter.save(pending1)
        chatRoomRepositoryAdapter.save(pending2)
        chatRoomRepositoryAdapter.save(assigned)

        when:
        def count = chatRoomRepositoryAdapter.countPendingSupportRooms()

        then:
        count == 2L
    }

    def "대기열이 비어있으면 빈 목록을 반환한다"() {
        when:
        def rooms = chatRoomRepositoryAdapter.findPendingSupportRooms(null, 10)

        then:
        rooms.isEmpty()
    }

    def "대기열이 비어있으면 0을 반환한다"() {
        when:
        def count = chatRoomRepositoryAdapter.countPendingSupportRooms()

        then:
        count == 0L
    }
}
