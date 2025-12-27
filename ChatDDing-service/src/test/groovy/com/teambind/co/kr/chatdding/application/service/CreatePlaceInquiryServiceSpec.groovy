package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.CreatePlaceInquiryCommand
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher
import com.teambind.co.kr.chatdding.common.exception.ChatException
import com.teambind.co.kr.chatdding.common.exception.ErrorCode
import com.teambind.co.kr.chatdding.common.util.generator.PrimaryKeyGenerator
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomContext
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import com.teambind.co.kr.chatdding.domain.event.InquiryCreatedEvent
import spock.lang.Specification
import spock.lang.Subject

class CreatePlaceInquiryServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    PrimaryKeyGenerator primaryKeyGenerator = Mock()
    EventPublisher eventPublisher = Mock()
    SendMessageUseCase sendMessageUseCase = Mock()

    @Subject
    CreatePlaceInquiryService service

    def setup() {
        service = new CreatePlaceInquiryService(
                chatRoomRepository,
                primaryKeyGenerator,
                eventPublisher,
                sendMessageUseCase
        )
    }

    def "공간 문의 채팅방을 성공적으로 생성한다"() {
        given:
        def command = CreatePlaceInquiryCommand.of(100L, 200L, 12345L, "강남 스터디룸 A")

        primaryKeyGenerator.generateLongKey() >> 999L
        chatRoomRepository.findPlaceInquiryByPlaceIdAndGuestId(12345L, UserId.of(100L)) >> Optional.empty()
        chatRoomRepository.save(_) >> { ChatRoom room -> room }

        when:
        def result = service.execute(command)

        then:
        result.roomId() != null
        result.type() == "PLACE_INQUIRY"
        result.context().contextType() == "PLACE"
        result.context().contextId() == 12345L
        result.context().contextName() == "강남 스터디룸 A"

        and:
        1 * eventPublisher.publish(_ as InquiryCreatedEvent)
    }

    def "초기 메시지가 있으면 메시지도 함께 전송한다"() {
        given:
        def command = CreatePlaceInquiryCommand.of(100L, 200L, 12345L, "강남 스터디룸 A", "예약 문의드립니다")

        primaryKeyGenerator.generateLongKey() >> 999L
        chatRoomRepository.findPlaceInquiryByPlaceIdAndGuestId(12345L, UserId.of(100L)) >> Optional.empty()
        chatRoomRepository.save(_) >> { ChatRoom room -> room }

        when:
        service.execute(command)

        then:
        1 * sendMessageUseCase.execute(_)
        1 * eventPublisher.publish(_ as InquiryCreatedEvent)
    }

    def "중복 문의가 있으면 예외가 발생한다"() {
        given:
        def command = CreatePlaceInquiryCommand.of(100L, 200L, 12345L, "강남 스터디룸 A")

        def existingRoom = ChatRoom.createPlaceInquiry(
                RoomId.of(888L),
                UserId.of(100L),
                UserId.of(200L),
                ChatRoomContext.forPlace(12345L, "강남 스터디룸 A")
        )
        chatRoomRepository.findPlaceInquiryByPlaceIdAndGuestId(12345L, UserId.of(100L)) >> Optional.of(existingRoom)

        when:
        service.execute(command)

        then:
        def ex = thrown(ChatException)
        ex.errorCode == ErrorCode.DUPLICATE_INQUIRY
    }

    def "guestId가 null이면 예외가 발생한다"() {
        when:
        CreatePlaceInquiryCommand.of(null, 200L, 12345L, "테스트")

        then:
        thrown(IllegalArgumentException)
    }

    def "placeId가 0 이하이면 예외가 발생한다"() {
        when:
        CreatePlaceInquiryCommand.of(100L, 200L, 0L, "테스트")

        then:
        thrown(IllegalArgumentException)
    }

    def "placeName이 비어있으면 예외가 발생한다"() {
        when:
        CreatePlaceInquiryCommand.of(100L, 200L, 12345L, "")

        then:
        thrown(IllegalArgumentException)
    }
}
