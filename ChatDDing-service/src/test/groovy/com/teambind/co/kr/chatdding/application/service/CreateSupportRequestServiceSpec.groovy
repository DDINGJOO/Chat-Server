package com.teambind.co.kr.chatdding.application.service

import com.teambind.co.kr.chatdding.application.port.in.CreateSupportRequestCommand
import com.teambind.co.kr.chatdding.application.port.in.SendMessageUseCase
import com.teambind.co.kr.chatdding.application.port.out.EventPublisher
import com.teambind.co.kr.chatdding.common.util.generator.PrimaryKeyGenerator
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository
import com.teambind.co.kr.chatdding.domain.event.SupportRequestCreatedEvent
import spock.lang.Specification
import spock.lang.Subject

class CreateSupportRequestServiceSpec extends Specification {

    ChatRoomRepository chatRoomRepository = Mock()
    PrimaryKeyGenerator primaryKeyGenerator = Mock()
    EventPublisher eventPublisher = Mock()
    SendMessageUseCase sendMessageUseCase = Mock()

    @Subject
    CreateSupportRequestService service

    def setup() {
        service = new CreateSupportRequestService(
                chatRoomRepository,
                primaryKeyGenerator,
                eventPublisher,
                sendMessageUseCase
        )
    }

    def "상담 요청 채팅방을 성공적으로 생성한다"() {
        given:
        def command = CreateSupportRequestCommand.of(100L)

        primaryKeyGenerator.generateLongKey() >> 999L
        chatRoomRepository.save(_) >> { ChatRoom room -> room }

        when:
        def result = service.execute(command)

        then:
        result.roomId() != null
        result.status() == "ACTIVE"
        result.category() == null

        and:
        1 * eventPublisher.publish(_ as SupportRequestCreatedEvent)
    }

    def "카테고리와 함께 상담 요청을 생성한다"() {
        given:
        def command = CreateSupportRequestCommand.of(100L, "결제 문의")

        primaryKeyGenerator.generateLongKey() >> 999L
        chatRoomRepository.save(_) >> { ChatRoom room -> room }

        when:
        def result = service.execute(command)

        then:
        result.category() == "결제 문의"

        and:
        1 * eventPublisher.publish({ SupportRequestCreatedEvent event ->
            event.category() == "결제 문의"
        })
    }

    def "초기 메시지가 있으면 메시지도 함께 전송한다"() {
        given:
        def command = CreateSupportRequestCommand.of(100L, "결제 문의", "환불 요청합니다")

        primaryKeyGenerator.generateLongKey() >> 999L
        chatRoomRepository.save(_) >> { ChatRoom room -> room }

        when:
        service.execute(command)

        then:
        1 * sendMessageUseCase.execute(_)
        1 * eventPublisher.publish(_ as SupportRequestCreatedEvent)
    }

    def "초기 메시지가 없으면 메시지 전송을 하지 않는다"() {
        given:
        def command = CreateSupportRequestCommand.of(100L)

        primaryKeyGenerator.generateLongKey() >> 999L
        chatRoomRepository.save(_) >> { ChatRoom room -> room }

        when:
        service.execute(command)

        then:
        0 * sendMessageUseCase.execute(_)
        1 * eventPublisher.publish(_ as SupportRequestCreatedEvent)
    }

    def "userId가 null이면 예외가 발생한다"() {
        when:
        CreateSupportRequestCommand.of(null)

        then:
        thrown(IllegalArgumentException)
    }
}
