package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.adapter;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.message.Message;
import com.teambind.co.kr.chatdding.domain.message.MessageId;
import com.teambind.co.kr.chatdding.domain.message.MessageRepository;
import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document.MessageDocument;
import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.repository.MessageMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Message Repository Adapter (Infrastructure Layer)
 *
 * <p>도메인 Repository 인터페이스를 MongoDB로 구현</p>
 */
@Repository
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Message save(Message message) {
        MessageDocument document = MessageDocument.from(message);
        MessageDocument saved = mongoRepository.save(document);
        return saved.toDomain();
    }

    @Override
    public Optional<Message> findById(MessageId messageId) {
        return mongoRepository.findById(messageId.getValue())
                .map(MessageDocument::toDomain);
    }

    @Override
    public List<Message> findByRoomIdOrderByCreatedAtDesc(RoomId roomId, int limit, int offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return mongoRepository.findByRoomIdOrderByCreatedAtDesc(roomId.getValue(), pageable)
                .stream()
                .map(MessageDocument::toDomain)
                .toList();
    }

    @Override
    public List<Message> findByRoomIdBeforeCursor(RoomId roomId, MessageId cursorId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return mongoRepository.findByRoomIdAndIdLessThanOrderByCreatedAtDesc(
                        roomId.getValue(), cursorId.getValue(), pageable)
                .stream()
                .map(MessageDocument::toDomain)
                .toList();
    }

    @Override
    public List<Message> findByRoomIdAndCreatedAtAfter(RoomId roomId, LocalDateTime after) {
        return mongoRepository.findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(
                        roomId.getValue(), after)
                .stream()
                .map(MessageDocument::toDomain)
                .toList();
    }

    @Override
    public long countUnreadByRoomIdAndUserId(RoomId roomId, UserId userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId.getValue()));
        query.addCriteria(Criteria.where("readBy." + userId.getValue()).exists(false));
        return mongoTemplate.count(query, MessageDocument.class);
    }

    @Override
    public Optional<Message> findLatestByRoomId(RoomId roomId) {
        return mongoRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId.getValue())
                .map(MessageDocument::toDomain);
    }

    @Override
    public void deleteById(MessageId messageId) {
        mongoRepository.deleteById(messageId.getValue());
    }

    @Override
    public void deleteAllByRoomId(RoomId roomId) {
        mongoRepository.deleteAllByRoomId(roomId.getValue());
    }
}
