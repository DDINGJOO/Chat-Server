package com.teambind.co.kr.chatdding.infrastructure.cache.redis;

import com.teambind.co.kr.chatdding.application.port.out.UnreadCountCachePort;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 안읽은 메시지 수 캐시 Adapter
 *
 * <p>Cache-Aside 패턴 적용</p>
 * <p>Graceful Degradation: Redis 장애 시 Optional.empty() 반환하여 DB 조회 유도</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnreadCountRedisAdapter implements UnreadCountCachePort {

    private static final String KEY_PREFIX = "unread:";
    private static final long TTL_HOURS = 24;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Optional<Integer> getUnreadCount(RoomId roomId, UserId userId) {
        try {
            String key = buildKey(roomId, userId);
            Object value = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(value)
                    .map(v -> ((Number) v).intValue());
        } catch (Exception e) {
            log.warn("Redis getUnreadCount failed, returning empty. key=unread:{}:{}, error={}",
                    roomId.toStringValue(), userId.getValue(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void setUnreadCount(RoomId roomId, UserId userId, int count) {
        try {
            String key = buildKey(roomId, userId);
            redisTemplate.opsForValue().set(key, count, TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cache set: {}={}", key, count);
        } catch (Exception e) {
            log.warn("Redis setUnreadCount failed. key=unread:{}:{}, count={}, error={}",
                    roomId.toStringValue(), userId.getValue(), count, e.getMessage());
        }
    }

    @Override
    public void incrementUnreadCount(RoomId roomId, UserId userId) {
        try {
            String key = buildKey(roomId, userId);
            Long newValue = redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cache incremented: {}={}", key, newValue);
        } catch (Exception e) {
            log.warn("Redis incrementUnreadCount failed. key=unread:{}:{}, error={}",
                    roomId.toStringValue(), userId.getValue(), e.getMessage());
        }
    }

    @Override
    public void resetUnreadCount(RoomId roomId, UserId userId) {
        try {
            String key = buildKey(roomId, userId);
            redisTemplate.opsForValue().set(key, 0, TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cache reset: {}=0", key);
        } catch (Exception e) {
            log.warn("Redis resetUnreadCount failed. key=unread:{}:{}, error={}",
                    roomId.toStringValue(), userId.getValue(), e.getMessage());
        }
    }

    @Override
    public Map<RoomId, Integer> getUnreadCounts(List<RoomId> roomIds, UserId userId) {
        Map<RoomId, Integer> result = new HashMap<>();

        try {
            List<String> keys = roomIds.stream()
                    .map(roomId -> buildKey(roomId, userId))
                    .toList();

            List<Object> values = redisTemplate.opsForValue().multiGet(keys);

            if (values != null) {
                for (int i = 0; i < roomIds.size(); i++) {
                    Object value = values.get(i);
                    if (value != null) {
                        result.put(roomIds.get(i), ((Number) value).intValue());
                    }
                }
            }

            log.debug("Cache multiGet: {} keys requested, {} hits", roomIds.size(), result.size());
        } catch (Exception e) {
            log.warn("Redis getUnreadCounts failed, returning empty map. userId={}, error={}",
                    userId.getValue(), e.getMessage());
        }

        return result;
    }

    private String buildKey(RoomId roomId, UserId userId) {
        return KEY_PREFIX + roomId.toStringValue() + ":" + userId.getValue();
    }
}
