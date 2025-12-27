package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document;

import com.teambind.co.kr.chatdding.domain.chatroom.RoomId;
import com.teambind.co.kr.chatdding.domain.common.UserId;
import com.teambind.co.kr.chatdding.domain.message.Message;
import com.teambind.co.kr.chatdding.domain.message.MessageId;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 메시지 MongoDB Document
 */
@Document(collection = "messages")
@CompoundIndexes({
        @CompoundIndex(name = "idx_roomId_createdAt", def = "{'roomId': 1, 'createdAt': -1}")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDocument {

    @Id
    private Long id;

    @Indexed
    private Long roomId;

    private Long senderId;

    private String content;

    private Map<Long, LocalDateTime> readBy;

    private Set<Long> deletedBy;

    @Indexed
    private LocalDateTime createdAt;

    public static MessageDocument from(Message message) {
        Map<Long, LocalDateTime> readByMap = message.getReadBy().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getValue(),
                        Map.Entry::getValue
                ));

        Set<Long> deletedBySet = message.getDeletedBy().stream()
                .map(UserId::getValue)
                .collect(Collectors.toSet());

        return MessageDocument.builder()
                .id(message.getId().getValue())
                .roomId(message.getRoomId().getValue())
                .senderId(message.getSenderId().getValue())
                .content(message.getContent())
                .readBy(readByMap)
                .deletedBy(deletedBySet)
                .createdAt(message.getCreatedAt())
                .build();
    }

    public Message toDomain() {
        Map<UserId, LocalDateTime> domainReadBy = new HashMap<>();
        if (readBy != null) {
            readBy.forEach((userId, readAt) ->
                    domainReadBy.put(UserId.of(userId), readAt));
        }

        Set<UserId> domainDeletedBy = new HashSet<>();
        if (deletedBy != null) {
            deletedBy.forEach(userId ->
                    domainDeletedBy.add(UserId.of(userId)));
        }

        return Message.restore(
                MessageId.of(id),
                RoomId.of(roomId),
                UserId.of(senderId),
                content,
                domainReadBy,
                domainDeletedBy,
                createdAt
        );
    }
}
