package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoom;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomContext;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomStatus;
import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomType;
import com.teambind.co.kr.chatdding.domain.chatroom.Participant;
import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 MongoDB Document
 */
@Document(collection = "chat_rooms")
@CompoundIndexes({
        @CompoundIndex(name = "idx_type_participantIds", def = "{'type': 1, 'sortedParticipantIds': 1}"),
        @CompoundIndex(name = "idx_participantIds_lastMessageAt", def = "{'participantIds': 1, 'lastMessageAt': -1}")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDocument {

    @Id
    private Long id;

    private ChatRoomType type;

    private String name;

    private List<ParticipantDocument> participants;

    @Indexed
    private List<Long> participantIds;

    private List<Long> sortedParticipantIds;

    private Long ownerId;

    private ChatRoomStatus status;

    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime lastMessageAt;

    private ChatRoomContextDocument context;

    public static ChatRoomDocument from(ChatRoom chatRoom) {
        List<ParticipantDocument> participantDocs = chatRoom.getParticipants().stream()
                .map(ParticipantDocument::from)
                .toList();

        List<Long> participantIds = chatRoom.getParticipantIds().stream()
                .map(UserId::getValue)
                .toList();

        return ChatRoomDocument.builder()
                .id(chatRoom.getId().getValue())
                .type(chatRoom.getType())
                .name(chatRoom.getName())
                .participants(participantDocs)
                .participantIds(participantIds)
                .sortedParticipantIds(chatRoom.getSortedParticipantIdValues())
                .ownerId(chatRoom.getOwnerId().getValue())
                .status(chatRoom.getStatus())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .context(ChatRoomContextDocument.from(chatRoom.getContext()))
                .build();
    }

    public ChatRoom toDomain() {
        List<Participant> domainParticipants = participants.stream()
                .map(ParticipantDocument::toDomain)
                .toList();

        ChatRoomContext domainContext = context != null ? context.toDomain() : null;

        return ChatRoom.restore(
                RoomId.of(id),
                type,
                name,
                domainParticipants,
                UserId.of(ownerId),
                status,
                createdAt,
                lastMessageAt,
                domainContext
        );
    }
}
