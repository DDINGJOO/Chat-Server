package com.teambind.co.kr.chatdding;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EmbeddedKafka(
		partitions = 1,
		topics = {"chat-message-sent", "chat-message-read"},
		brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"}
)
@ActiveProfiles("test")
class ChatDDingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
