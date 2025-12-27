package com.teambind.co.kr.chatdding.domain.chatroom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 채팅방 컨텍스트 Value Object
 *
 * <p>채팅방이 특정 도메인(공간, 주문, 예약 등)과 연결될 때 해당 정보를 저장</p>
 */
public record ChatRoomContext(
        ContextType contextType,
        Long contextId,
        String contextName,
        Map<String, Object> metadata
) {

    public ChatRoomContext {
        Objects.requireNonNull(contextType, "contextType must not be null");
        Objects.requireNonNull(contextId, "contextId must not be null");
        if (contextId <= 0) {
            throw new IllegalArgumentException("contextId must be positive");
        }
        metadata = metadata != null ? Collections.unmodifiableMap(new HashMap<>(metadata)) : Map.of();
    }

    /**
     * 공간 문의용 컨텍스트 생성
     */
    public static ChatRoomContext forPlace(Long placeId, String placeName) {
        return new ChatRoomContext(ContextType.PLACE, placeId, placeName, Map.of());
    }

    /**
     * 공간 문의용 컨텍스트 생성 (메타데이터 포함)
     */
    public static ChatRoomContext forPlace(Long placeId, String placeName, Map<String, Object> metadata) {
        return new ChatRoomContext(ContextType.PLACE, placeId, placeName, metadata);
    }

    /**
     * 주문 관련 컨텍스트 생성
     */
    public static ChatRoomContext forOrder(Long orderId, String orderName) {
        return new ChatRoomContext(ContextType.ORDER, orderId, orderName, Map.of());
    }

    /**
     * 예약 관련 컨텍스트 생성
     */
    public static ChatRoomContext forBooking(Long bookingId, String bookingName) {
        return new ChatRoomContext(ContextType.BOOKING, bookingId, bookingName, Map.of());
    }

    /**
     * 공간 컨텍스트인지 확인
     */
    public boolean isPlaceContext() {
        return contextType == ContextType.PLACE;
    }
}
