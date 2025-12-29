package com.teambind.co.kr.chatdding.infrastructure.cache.redis

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId
import com.teambind.co.kr.chatdding.domain.common.UserId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import spock.lang.Specification
import spock.lang.Subject

class UnreadCountRedisAdapterSpec extends Specification {

    RedisTemplate<String, Object> redisTemplate = Mock()
    ValueOperations<String, Object> valueOperations = Mock()

    @Subject
    UnreadCountRedisAdapter adapter = new UnreadCountRedisAdapter(redisTemplate)

    def roomId = RoomId.of(123L)
    def userId = UserId.of(456L)
    def expectedKey = "unread:123:456"

    def setup() {
        redisTemplate.opsForValue() >> valueOperations
    }

    def "getUnreadCount - 캐시 히트 시 값 반환"() {
        given:
        valueOperations.get(expectedKey) >> 5

        when:
        def result = adapter.getUnreadCount(roomId, userId)

        then:
        result.isPresent()
        result.get() == 5
    }

    def "getUnreadCount - 캐시 미스 시 Optional.empty 반환"() {
        given:
        valueOperations.get(expectedKey) >> null

        when:
        def result = adapter.getUnreadCount(roomId, userId)

        then:
        result.isEmpty()
    }

    def "getUnreadCount - Redis 장애 시 Optional.empty 반환 (Graceful Degradation)"() {
        given:
        valueOperations.get(expectedKey) >> { throw new RuntimeException("Redis connection failed") }

        when:
        def result = adapter.getUnreadCount(roomId, userId)

        then:
        result.isEmpty()
        noExceptionThrown()
    }

    def "setUnreadCount - 값 저장 및 TTL 설정"() {
        when:
        adapter.setUnreadCount(roomId, userId, 10)

        then:
        1 * valueOperations.set(expectedKey, 10, 24, _)
    }

    def "setUnreadCount - Redis 장애 시 예외 전파 없음"() {
        given:
        valueOperations.set(_, _, _, _) >> { throw new RuntimeException("Redis write failed") }

        when:
        adapter.setUnreadCount(roomId, userId, 10)

        then:
        noExceptionThrown()
    }

    def "incrementUnreadCount - 값 1 증가"() {
        when:
        adapter.incrementUnreadCount(roomId, userId)

        then:
        1 * valueOperations.increment(expectedKey)
        1 * redisTemplate.expire(expectedKey, 24, _)
    }

    def "incrementUnreadCount - Redis 장애 시 예외 전파 없음"() {
        given:
        valueOperations.increment(_) >> { throw new RuntimeException("Redis increment failed") }

        when:
        adapter.incrementUnreadCount(roomId, userId)

        then:
        noExceptionThrown()
    }

    def "resetUnreadCount - 값 0으로 설정"() {
        when:
        adapter.resetUnreadCount(roomId, userId)

        then:
        1 * valueOperations.set(expectedKey, 0, 24, _)
    }

    def "getUnreadCounts - 여러 키 일괄 조회"() {
        given:
        def roomId1 = RoomId.of(1L)
        def roomId2 = RoomId.of(2L)
        def roomId3 = RoomId.of(3L)
        def roomIds = [roomId1, roomId2, roomId3]

        valueOperations.multiGet(_) >> [5, null, 10]

        when:
        def result = adapter.getUnreadCounts(roomIds, userId)

        then:
        result.size() == 2
        result.get(roomId1) == 5
        result.get(roomId3) == 10
        !result.containsKey(roomId2)
    }

    def "getUnreadCounts - Redis 장애 시 빈 맵 반환"() {
        given:
        def roomIds = [RoomId.of(1L), RoomId.of(2L)]
        valueOperations.multiGet(_) >> { throw new RuntimeException("Redis multiGet failed") }

        when:
        def result = adapter.getUnreadCounts(roomIds, userId)

        then:
        result.isEmpty()
        noExceptionThrown()
    }
}
