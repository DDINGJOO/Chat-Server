package com.teambind.co.kr.chatdding

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("test")
class ChatDDingServiceApplicationSpec extends Specification {

    def "Application context loads successfully"() {
        expect:
        true
    }
}
