package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.repository;

import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document.MessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Message Spring Data MongoDB Repository
 */
public interface MessageMongoRepository extends MongoRepository<MessageDocument, Long> {

    List<MessageDocument> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    @Query("{ 'roomId': ?0, '_id': { $lt: ?1 } }")
    List<MessageDocument> findByRoomIdAndIdLessThanOrderByCreatedAtDesc(
            Long roomId, Long cursorId, Pageable pageable);

    List<MessageDocument> findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(
            Long roomId, LocalDateTime after);

    @Query(value = "{ 'roomId': ?0, 'readBy.?1': { $exists: false } }", count = true)
    long countByRoomIdAndUserIdNotInReadBy(Long roomId, Long userId);

    Optional<MessageDocument> findFirstByRoomIdOrderByCreatedAtDesc(Long roomId);

    void deleteAllByRoomId(Long roomId);
}
