package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.repository;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;
import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document.ChatRoomDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatRoom Custom Repository Implementation
 *
 * <p>MongoTemplate을 사용한 커스텀 쿼리 구현</p>
 */
@Repository
@RequiredArgsConstructor
public class ChatRoomMongoRepositoryCustomImpl implements ChatRoomMongoRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<ChatRoomDocument> findPendingSupportRooms(String cursor, int limit) {
        Query query = new Query();

        // SUPPORT 타입, ACTIVE 상태, 참여자 1명 (상담원 미배정)
        query.addCriteria(Criteria.where("type").is(ChatRoomType.SUPPORT));
        query.addCriteria(Criteria.where("status").is(ChatRoomStatus.ACTIVE));
        query.addCriteria(Criteria.where("participantIds").size(1));

        // 커서 기반 페이지네이션 (id 기준)
        if (cursor != null && !cursor.isBlank()) {
            long cursorId = Long.parseLong(cursor);
            query.addCriteria(Criteria.where("_id").gt(cursorId));
        }

        // 생성일 오름차순 (오래된 것 우선)
        query.with(Sort.by(Sort.Direction.ASC, "createdAt", "_id"));
        query.limit(limit);

        return mongoTemplate.find(query, ChatRoomDocument.class);
    }

    @Override
    public long countPendingSupportRooms() {
        Query query = new Query();

        query.addCriteria(Criteria.where("type").is(ChatRoomType.SUPPORT));
        query.addCriteria(Criteria.where("status").is(ChatRoomStatus.ACTIVE));
        query.addCriteria(Criteria.where("participantIds").size(1));

        return mongoTemplate.count(query, ChatRoomDocument.class);
    }
}
