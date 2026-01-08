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
public interface ChatRoomMongoRepository extends MongoRepository<ChatRoomDocument, Long>, ChatRoomMongoRepositoryCustom {

    List<ChatRoomDocument> findByParticipantIdsContaining(Long userId);

    List<ChatRoomDocument> findByParticipantIdsContainingAndStatusOrderByLastMessageAtDesc(
            Long userId, ChatRoomStatus status);

    List<ChatRoomDocument> findByParticipantIdsContainingAndStatusAndTypeOrderByLastMessageAtDesc(
            Long userId, ChatRoomStatus status, ChatRoomType type);

    Optional<ChatRoomDocument> findByTypeAndSortedParticipantIds(
            ChatRoomType type, List<Long> sortedParticipantIds);

    /**
     * 공간 문의 중복 체크: 동일 게스트-공간 조합 조회
     */
    Optional<ChatRoomDocument> findByTypeAndContext_ContextIdAndParticipantIdsContaining(
            ChatRoomType type, Long contextId, Long participantId);

    /**
     * 호스트의 공간 문의 목록 조회
     */
    List<ChatRoomDocument> findByTypeAndOwnerIdOrderByLastMessageAtDesc(
            ChatRoomType type, Long ownerId);

    /**
     * 호스트의 특정 공간 문의 목록 조회
     */
    List<ChatRoomDocument> findByTypeAndOwnerIdAndContext_ContextIdOrderByLastMessageAtDesc(
            ChatRoomType type, Long ownerId, Long contextId);

}
