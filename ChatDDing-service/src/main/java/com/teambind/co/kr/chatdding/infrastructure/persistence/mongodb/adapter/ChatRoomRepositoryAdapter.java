package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.adapter;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomRepository;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document.ChatRoomDocument;
import com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.repository.ChatRoomMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ChatRoom Repository Adapter (Infrastructure Layer)
 *
 * <p>도메인 Repository 인터페이스를 MongoDB로 구현</p>
 */
@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryAdapter implements ChatRoomRepository {

    private final ChatRoomMongoRepository mongoRepository;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        ChatRoomDocument document = ChatRoomDocument.from(chatRoom);
        ChatRoomDocument saved = mongoRepository.save(document);
        return saved.toDomain();
    }

    @Override
    public Optional<ChatRoom> findById(RoomId roomId) {
        return mongoRepository.findById(roomId.getValue())
                .map(ChatRoomDocument::toDomain);
    }

    @Override
    public List<ChatRoom> findByParticipantUserId(UserId userId) {
        return mongoRepository.findByParticipantIdsContaining(userId.getValue())
                .stream()
                .map(ChatRoomDocument::toDomain)
                .toList();
    }

    @Override
    public List<ChatRoom> findActiveByParticipantUserIdOrderByLastMessageAtDesc(UserId userId) {
        return mongoRepository.findByParticipantIdsContainingAndStatusOrderByLastMessageAtDesc(
                        userId.getValue(), ChatRoomStatus.ACTIVE)
                .stream()
                .map(ChatRoomDocument::toDomain)
                .toList();
    }

    @Override
    public List<ChatRoom> findActiveByParticipantUserIdAndTypeOrderByLastMessageAtDesc(UserId userId, ChatRoomType type) {
        return mongoRepository.findByParticipantIdsContainingAndStatusAndTypeOrderByLastMessageAtDesc(
                        userId.getValue(), ChatRoomStatus.ACTIVE, type)
                .stream()
                .map(ChatRoomDocument::toDomain)
                .toList();
    }

    @Override
    public Optional<ChatRoom> findDmByParticipantIds(List<Long> participantIds) {
        return mongoRepository.findByTypeAndSortedParticipantIds(ChatRoomType.DM, participantIds)
                .map(ChatRoomDocument::toDomain);
    }

    @Override
    public boolean existsById(RoomId roomId) {
        return mongoRepository.existsById(roomId.getValue());
    }

    @Override
    public void deleteById(RoomId roomId) {
        mongoRepository.deleteById(roomId.getValue());
    }

    @Override
    public Optional<ChatRoom> findPlaceInquiryByPlaceIdAndGuestId(Long placeId, UserId guestId) {
        return mongoRepository.findByTypeAndContext_ContextIdAndParticipantIdsContaining(
                        ChatRoomType.PLACE_INQUIRY, placeId, guestId.getValue())
                .map(ChatRoomDocument::toDomain);
    }

    @Override
    public List<ChatRoom> findPlaceInquiriesByHostId(UserId hostId, Long placeId) {
        if (placeId != null) {
            return mongoRepository.findByTypeAndOwnerIdAndContext_ContextIdOrderByLastMessageAtDesc(
                            ChatRoomType.PLACE_INQUIRY, hostId.getValue(), placeId)
                    .stream()
                    .map(ChatRoomDocument::toDomain)
                    .toList();
        }
        return mongoRepository.findByTypeAndOwnerIdOrderByLastMessageAtDesc(
                        ChatRoomType.PLACE_INQUIRY, hostId.getValue())
                .stream()
                .map(ChatRoomDocument::toDomain)
                .toList();
    }

    @Override
    public List<ChatRoom> findPendingSupportRooms(String cursor, int limit) {
        return mongoRepository.findPendingSupportRooms(cursor, limit)
                .stream()
                .map(ChatRoomDocument::toDomain)
                .toList();
    }

    @Override
    public long countPendingSupportRooms() {
        return mongoRepository.countPendingSupportRooms();
    }
}
