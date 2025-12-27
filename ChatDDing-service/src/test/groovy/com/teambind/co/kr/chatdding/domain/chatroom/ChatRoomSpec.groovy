package com.teambind.co.kr.chatdding.domain.chatroom

import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.domain.common.UserId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

class ChatRoomSpec extends Specification {

    // ========================
    // DM 채팅방 생성 테스트
    // ========================

    def "createDm()으로 DM 채팅방을 생성할 수 있다"() {
        given:
        def roomId = RoomId.of(1L)
        def senderId = UserId.of(100L)
        def recipientId = UserId.of(200L)

        when:
        def chatRoom = ChatRoom.createDm(roomId, senderId, recipientId)

        then:
        chatRoom.id == roomId
        chatRoom.type == ChatRoomType.DM
        chatRoom.name == null
        chatRoom.ownerId == senderId
        chatRoom.status == ChatRoomStatus.ACTIVE
        chatRoom.participants.size() == 2
        chatRoom.isActive() == true
    }

    def "DM 채팅방의 참여자 ID가 올바르게 설정된다"() {
        given:
        def senderId = UserId.of(100L)
        def recipientId = UserId.of(200L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), senderId, recipientId)

        expect:
        chatRoom.participantIds.containsAll([senderId, recipientId])
        chatRoom.isParticipant(senderId) == true
        chatRoom.isParticipant(recipientId) == true
    }

    def "DM 채팅방의 sortedParticipantIdValues()는 정렬된 ID 목록을 반환한다"() {
        given:
        def senderId = UserId.of(200L)
        def recipientId = UserId.of(100L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), senderId, recipientId)

        expect:
        chatRoom.sortedParticipantIdValues == [100L, 200L]
    }

    // ========================
    // 그룹 채팅방 생성 테스트
    // ========================

    def "createGroup()으로 그룹 채팅방을 생성할 수 있다"() {
        given:
        def roomId = RoomId.of(1L)
        def ownerId = UserId.of(100L)
        def memberIds = [UserId.of(200L), UserId.of(300L)]
        def name = "테스트 그룹"

        when:
        def chatRoom = ChatRoom.createGroup(roomId, ownerId, memberIds, name)

        then:
        chatRoom.id == roomId
        chatRoom.type == ChatRoomType.GROUP
        chatRoom.name == name
        chatRoom.ownerId == ownerId
        chatRoom.status == ChatRoomStatus.ACTIVE
        chatRoom.participants.size() == 3
    }

    def "그룹 채팅방은 이름을 변경할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createGroup(
                RoomId.of(1L),
                UserId.of(100L),
                [UserId.of(200L)],
                "원래 이름"
        )

        when:
        chatRoom.changeName("새 이름")

        then:
        chatRoom.name == "새 이름"
    }

    def "그룹 채팅방 최대 인원(100명)을 초과하면 예외가 발생한다"() {
        given:
        def ownerId = UserId.of(1L)
        def memberIds = (2L..101L).collect { UserId.of(it) } // 100명 멤버 + 1명 오너 = 101명

        when:
        ChatRoom.createGroup(RoomId.of(1L), ownerId, memberIds, "대규모 그룹")

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.GROUP_MAX_PARTICIPANTS_EXCEEDED
    }

    // ========================
    // 상담 채팅방 생성 테스트
    // ========================

    def "createSupport()로 상담 채팅방을 생성할 수 있다"() {
        given:
        def roomId = RoomId.of(1L)
        def userId = UserId.of(100L)

        when:
        def chatRoom = ChatRoom.createSupport(roomId, userId)

        then:
        chatRoom.id == roomId
        chatRoom.type == ChatRoomType.SUPPORT
        chatRoom.name == null
        chatRoom.ownerId == userId
        chatRoom.participants.size() == 1
    }

    def "상담 채팅방에 상담원을 배정할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        def agentId = UserId.of(999L)

        when:
        chatRoom.assignAgent(agentId)

        then:
        chatRoom.participants.size() == 2
        chatRoom.isParticipant(agentId) == true
    }

    def "상담 채팅방에 이미 상담원이 있으면 추가 배정 시 예외가 발생한다"() {
        given:
        def chatRoom = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        chatRoom.assignAgent(UserId.of(999L))

        when:
        chatRoom.assignAgent(UserId.of(888L))

        then:
        thrown(IllegalStateException)
    }

    // ========================
    // 공통 기능 테스트
    // ========================

    def "DM 채팅방은 이름을 변경할 수 없다"() {
        given:
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(100L), UserId.of(200L))

        when:
        chatRoom.changeName("새 이름")

        then:
        thrown(IllegalStateException)
    }

    def "SUPPORT가 아닌 채팅방에서 상담원 배정을 시도하면 예외가 발생한다"() {
        given:
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(100L), UserId.of(200L))

        when:
        chatRoom.assignAgent(UserId.of(999L))

        then:
        thrown(IllegalStateException)
    }

    def "findParticipant()로 특정 참여자를 찾을 수 있다"() {
        given:
        def senderId = UserId.of(100L)
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), senderId, UserId.of(200L))

        when:
        def participant = chatRoom.findParticipant(senderId)

        then:
        participant.isPresent()
        participant.get().userId == senderId
    }

    def "findParticipant()로 없는 참여자를 찾으면 empty를 반환한다"() {
        given:
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(100L), UserId.of(200L))
        def nonParticipant = UserId.of(999L)

        when:
        def participant = chatRoom.findParticipant(nonParticipant)

        then:
        participant.isEmpty()
    }

    def "updateLastMessageAt()으로 마지막 메시지 시간을 업데이트할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(100L), UserId.of(200L))
        def originalTime = chatRoom.lastMessageAt
        def newTime = originalTime.plusMinutes(10)

        when:
        chatRoom.updateLastMessageAt(newTime)

        then:
        chatRoom.lastMessageAt == newTime
    }

    def "updateLastMessageAt()은 null을 무시한다"() {
        given:
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(100L), UserId.of(200L))
        def originalTime = chatRoom.lastMessageAt

        when:
        chatRoom.updateLastMessageAt(null)

        then:
        chatRoom.lastMessageAt == originalTime
    }

    def "close()로 채팅방을 종료할 수 있다"() {
        given:
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(100L), UserId.of(200L))

        when:
        chatRoom.close()

        then:
        chatRoom.status == ChatRoomStatus.CLOSED
        chatRoom.isActive() == false
    }

    def "isParticipant()는 참여자가 아닌 경우 false를 반환한다"() {
        given:
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(100L), UserId.of(200L))
        def nonParticipant = UserId.of(999L)

        expect:
        chatRoom.isParticipant(nonParticipant) == false
    }

    // ========================
    // restore 테스트
    // ========================

    def "restore()로 기존 데이터를 복원할 수 있다"() {
        given:
        def roomId = RoomId.of(1L)
        def type = ChatRoomType.GROUP
        def name = "복원된 그룹"
        def participants = [
                Participant.create(UserId.of(100L)),
                Participant.create(UserId.of(200L))
        ]
        def ownerId = UserId.of(100L)
        def status = ChatRoomStatus.ACTIVE
        def createdAt = LocalDateTime.of(2024, 1, 1, 10, 0)
        def lastMessageAt = LocalDateTime.of(2024, 1, 1, 12, 0)

        when:
        def chatRoom = ChatRoom.restore(roomId, type, name, participants, ownerId, status, createdAt, lastMessageAt)

        then:
        chatRoom.id == roomId
        chatRoom.type == type
        chatRoom.name == name
        chatRoom.participants.size() == 2
        chatRoom.ownerId == ownerId
        chatRoom.status == status
        chatRoom.createdAt == createdAt
        chatRoom.lastMessageAt == lastMessageAt
    }

    def "참여자 목록은 불변이다"() {
        given:
        def chatRoom = ChatRoom.createDm(RoomId.of(1L), UserId.of(100L), UserId.of(200L))

        when:
        chatRoom.participants.add(Participant.create(UserId.of(300L)))

        then:
        thrown(UnsupportedOperationException)
    }
}
