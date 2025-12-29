package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.GetSupportQueueQuery
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import spock.lang.Specification
import spock.lang.Subject

class GetSupportQueueServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()

    @Subject
    GetSupportQueueService service

    def setup() {
        service = new GetSupportQueueService(chatRoomRepository)
    }

    def "대기 중인 상담 목록을 조회한다"() {
        given:
        def query = GetSupportQueueQuery.of(10)
        def room1 = ChatRoom.createSupport(RoomId.of(1L), UserId.of(100L))
        def room2 = ChatRoom.createSupport(RoomId.of(2L), UserId.of(200L))

        chatRoomRepository.findPendingSupportRooms(null, 11) >> [room1, room2]
        chatRoomRepository.countPendingSupportRooms() >> 2L

        when:
        def result = service.execute(query)

        then:
        result.items().size() == 2
        result.items()[0].roomId() == "1"
        result.items()[1].roomId() == "2"
        result.totalCount() == 2L
        result.nextCursor() == null
    }

    def "대기열이 비어있으면 빈 목록을 반환한다"() {
        given:
        def query = GetSupportQueueQuery.of(10)

        chatRoomRepository.findPendingSupportRooms(null, 11) >> []
        chatRoomRepository.countPendingSupportRooms() >> 0L

        when:
        def result = service.execute(query)

        then:
        result.items().isEmpty()
        result.totalCount() == 0L
        result.nextCursor() == null
    }

    def "페이지네이션이 정상 동작한다"() {
        given:
        def query = GetSupportQueueQuery.of(2)
        def rooms = (1L..3L).collect { ChatRoom.createSupport(RoomId.of(it), UserId.of(it * 100)) }

        chatRoomRepository.findPendingSupportRooms(null, 3) >> rooms
        chatRoomRepository.countPendingSupportRooms() >> 5L

        when:
        def result = service.execute(query)

        then:
        result.items().size() == 2
        result.nextCursor() == "2"
        result.hasMore() == true
    }

    def "커서를 사용하여 다음 페이지를 조회한다"() {
        given:
        def query = GetSupportQueueQuery.of("2", 2)
        def rooms = (3L..4L).collect { ChatRoom.createSupport(RoomId.of(it), UserId.of(it * 100)) }

        chatRoomRepository.findPendingSupportRooms("2", 3) >> rooms
        chatRoomRepository.countPendingSupportRooms() >> 5L

        when:
        def result = service.execute(query)

        then:
        result.items().size() == 2
        result.items()[0].roomId() == "3"
    }

    def "limit이 0이하이면 예외가 발생한다"() {
        when:
        GetSupportQueueQuery.of(0)

        then:
        thrown(IllegalArgumentException)
    }

    def "limit이 100 초과이면 예외가 발생한다"() {
        when:
        GetSupportQueueQuery.of(101)

        then:
        thrown(IllegalArgumentException)
    }
}
