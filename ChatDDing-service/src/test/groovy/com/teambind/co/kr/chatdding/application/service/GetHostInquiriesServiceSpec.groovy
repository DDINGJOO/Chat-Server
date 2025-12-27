package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.GetHostInquiriesQuery
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomContext
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.message.MessageRepository
import spock.lang.Specification
import spock.lang.Subject

class GetHostInquiriesServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    MessageRepository messageRepository = Mock()

    @Subject
    GetHostInquiriesService service

    def setup() {
        service = new GetHostInquiriesService(chatRoomRepository, messageRepository)
    }

    def "호스트의 문의 목록을 조회한다"() {
        given:
        def query = GetHostInquiriesQuery.of(200L)
        def chatRoom = ChatRoom.createPlaceInquiry(
                RoomId.of(1L),
                UserId.of(100L),
                UserId.of(200L),
                ChatRoomContext.forPlace(12345L, "강남 스터디룸 A")
        )

        chatRoomRepository.findPlaceInquiriesByHostId(UserId.of(200L), null) >> [chatRoom]
        messageRepository.countUnreadByRoomIdAndUserId(_, _) >> 3L

        when:
        def result = service.execute(query)

        then:
        result.inquiries().size() == 1
        result.inquiries()[0].guestId() == 100L
        result.inquiries()[0].context().contextId() == 12345L
        result.inquiries()[0].unreadCount() == 3
    }

    def "placeId로 필터링하여 조회한다"() {
        given:
        def query = GetHostInquiriesQuery.of(200L, 12345L)
        def chatRoom = ChatRoom.createPlaceInquiry(
                RoomId.of(1L),
                UserId.of(100L),
                UserId.of(200L),
                ChatRoomContext.forPlace(12345L, "강남 스터디룸 A")
        )

        chatRoomRepository.findPlaceInquiriesByHostId(UserId.of(200L), 12345L) >> [chatRoom]
        messageRepository.countUnreadByRoomIdAndUserId(_, _) >> 0L

        when:
        def result = service.execute(query)

        then:
        result.inquiries().size() == 1
        result.inquiries()[0].context().contextId() == 12345L
    }

    def "문의가 없으면 빈 목록을 반환한다"() {
        given:
        def query = GetHostInquiriesQuery.of(200L)

        chatRoomRepository.findPlaceInquiriesByHostId(UserId.of(200L), null) >> []

        when:
        def result = service.execute(query)

        then:
        result.inquiries().isEmpty()
        result.hasMore() == false
        result.nextCursor() == null
    }

    def "limit보다 많은 결과가 있으면 hasMore가 true이다"() {
        given:
        def query = GetHostInquiriesQuery.of(200L, null, null, 1)
        def chatRoom1 = ChatRoom.createPlaceInquiry(
                RoomId.of(1L),
                UserId.of(100L),
                UserId.of(200L),
                ChatRoomContext.forPlace(12345L, "스터디룸 A")
        )
        def chatRoom2 = ChatRoom.createPlaceInquiry(
                RoomId.of(2L),
                UserId.of(101L),
                UserId.of(200L),
                ChatRoomContext.forPlace(12346L, "스터디룸 B")
        )

        chatRoomRepository.findPlaceInquiriesByHostId(UserId.of(200L), null) >> [chatRoom1, chatRoom2]
        messageRepository.countUnreadByRoomIdAndUserId(_, _) >> 0L

        when:
        def result = service.execute(query)

        then:
        result.inquiries().size() == 1
        result.hasMore() == true
        result.nextCursor() != null
    }

    def "hostId가 null이면 예외가 발생한다"() {
        when:
        GetHostInquiriesQuery.of(null)

        then:
        thrown(IllegalArgumentException)
    }
}
