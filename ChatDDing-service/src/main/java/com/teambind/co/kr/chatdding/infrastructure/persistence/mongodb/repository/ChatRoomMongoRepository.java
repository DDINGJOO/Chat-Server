package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.repository;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;
import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document.ChatRoomDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * ChatRoom Spring Data MongoDB Repository
 */
public interface ChatRoomMongoRepository extends MongoRepository<ChatRoomDocument, Long> {

    List<ChatRoomDocument> findByParticipantIdsContaining(Long userId);

    List<ChatRoomDocument> findByParticipantIdsContainingAndStatusOrderByLastMessageAtDesc(
            Long userId, ChatRoomStatus status);

    Optional<ChatRoomDocument> findByTypeAndSortedParticipantIds(
            ChatRoomType type, List<Long> sortedParticipantIds);
}
