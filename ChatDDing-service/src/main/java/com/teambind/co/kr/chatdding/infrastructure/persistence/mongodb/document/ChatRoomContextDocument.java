package com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.document;

import com.teambind.co.kr.chatdding.domain.chatroom.ChatRoomContext;
import com.teambind.co.kr.chatdding.domain.chatroom.ContextType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 채팅방 컨텍스트 MongoDB Document
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomContextDocument {

    private String contextType;

    private Long contextId;

    private String contextName;

    private Map<String, Object> metadata;

    public static ChatRoomContextDocument from(ChatRoomContext context) {
        if (context == null) {
            return null;
        }
        return ChatRoomContextDocument.builder()
                .contextType(context.contextType().name())
                .contextId(context.contextId())
                .contextName(context.contextName())
                .metadata(context.metadata())
                .build();
    }

    public ChatRoomContext toDomain() {
        return new ChatRoomContext(
                ContextType.valueOf(contextType),
                contextId,
                contextName,
                metadata
        );
    }
}
