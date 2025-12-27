package com.teambind.co.kr.chatdding.infrastructure.messaging.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.teambind.co.kr.chatdding.domain.event.MessageReadEvent
import com.teambind.co.kr.chatdding.domain.event.MessageSentEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration
import java.time.LocalDateTime

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = ["chat-message-sent", "chat-message-read"],
        brokerProperties = ["listeners=PLAINTEXT://localhost:9093", "port=9093"]
)
@ActiveProfiles("test")
class KafkaEventPublisherSpec extends Specification {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate

    @Autowired
    ObjectMapper objectMapper

    @Subject
    KafkaEventPublisher kafkaEventPublisher

    def setup() {
        kafkaEventPublisher = new KafkaEventPublisher(kafkaTemplate, objectMapper)
    }

    def "MessageSentEvent를 chat-message-sent 토픽으로 발행한다"() {
        given:
        def event = new MessageSentEvent(
                "123",
                "456",
                1L,
                "테스트 메시지",
                "테스트 메시지",
                [2L, 3L],
                LocalDateTime.now()
        )

        def consumerProps = KafkaTestUtils.consumerProps("test-group-sent", "true", embeddedKafkaBroker)
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        def consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps)
        def consumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "chat-message-sent")

        when:
        kafkaEventPublisher.publish(event)

        then:
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5))
        records.count() >= 1

        def record = records.iterator().next()
        record.key() == "456"
        record.value().contains("MESSAGE_SENT")
        record.value().contains("테스트 메시지")

        cleanup:
        consumer?.close()
    }

    def "MessageReadEvent를 chat-message-read 토픽으로 발행한다"() {
        given:
        def event = MessageReadEvent.of("456", 1L, 5)

        def consumerProps = KafkaTestUtils.consumerProps("test-group-read", "true", embeddedKafkaBroker)
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        def consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps)
        def consumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "chat-message-read")

        when:
        kafkaEventPublisher.publish(event)

        then:
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5))
        records.count() >= 1

        def record = records.iterator().next()
        record.key() == "456"
        record.value().contains("MESSAGE_READ")

        cleanup:
        consumer?.close()
    }

    def "특정 토픽으로 이벤트를 발행할 수 있다"() {
        given:
        def event = new MessageSentEvent(
                "123",
                "456",
                1L,
                "커스텀 토픽 메시지",
                "커스텀 토픽 메시지",
                [2L],
                LocalDateTime.now()
        )

        def consumerProps = KafkaTestUtils.consumerProps("test-group-custom", "true", embeddedKafkaBroker)
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        def consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps)
        def consumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "chat-message-sent")

        when:
        kafkaEventPublisher.publish("chat-message-sent", event)

        then:
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5))
        records.count() >= 1

        cleanup:
        consumer?.close()
    }

    def "이벤트가 JSON 형식으로 직렬화된다"() {
        given:
        def event = new MessageSentEvent(
                "123",
                "456",
                1L,
                "테스트",
                "테스트",
                [2L],
                LocalDateTime.now()
        )

        def consumerProps = KafkaTestUtils.consumerProps("test-group-json", "true", embeddedKafkaBroker)
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        def consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps)
        def consumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "chat-message-sent")

        when:
        kafkaEventPublisher.publish(event)

        then:
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5))
        def record = records.iterator().next()

        def json = objectMapper.readTree(record.value())
        json.get("messageId").asText() == "123"
        json.get("roomId").asText() == "456"
        json.get("eventType").asText() == "MESSAGE_SENT"

        cleanup:
        consumer?.close()
    }
}
