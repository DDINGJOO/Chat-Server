package com.teambind.co.kr.chatdding.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ChatRoom Errors
    CHAT_ROOM_NOT_FOUND("CHAT_001", HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다"),
    ROOM_NOT_FOUND("CHAT_001", HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다"),
    CHAT_ROOM_ACCESS_DENIED("CHAT_002", HttpStatus.FORBIDDEN, "채팅방 접근 권한이 없습니다"),
    NOT_PARTICIPANT("CHAT_002", HttpStatus.FORBIDDEN, "채팅방 참여자가 아닙니다"),
    INVALID_CHAT_ROOM_TYPE("CHAT_003", HttpStatus.BAD_REQUEST, "잘못된 채팅방 유형입니다"),
    ROOM_ALREADY_CLOSED("CHAT_012", HttpStatus.CONFLICT, "이미 종료된 채팅방입니다"),

    // Message Errors
    MESSAGE_CONTENT_EMPTY("CHAT_004", HttpStatus.BAD_REQUEST, "메시지 내용이 비어있습니다"),
    MESSAGE_CONTENT_TOO_LONG("CHAT_005", HttpStatus.BAD_REQUEST, "메시지 길이가 초과되었습니다 (최대 5,000자)"),
    MESSAGE_NOT_FOUND("CHAT_006", HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다"),
    MESSAGE_DELETE_DENIED("CHAT_007", HttpStatus.FORBIDDEN, "메시지 삭제 권한이 없습니다"),

    // Recipient Errors
    RECIPIENT_NOT_SPECIFIED("CHAT_008", HttpStatus.BAD_REQUEST, "수신자가 지정되지 않았습니다"),
    GROUP_MAX_PARTICIPANTS_EXCEEDED("CHAT_009", HttpStatus.BAD_REQUEST, "그룹 채팅 최대 인원을 초과했습니다"),

    // Support Errors
    SUPPORT_ALREADY_IN_PROGRESS("CHAT_010", HttpStatus.CONFLICT, "이미 진행 중인 상담이 있습니다"),
    NOT_SUPPORT_ROOM("CHAT_013", HttpStatus.BAD_REQUEST, "상담 채팅방이 아닙니다"),
    AGENT_ALREADY_ASSIGNED("CHAT_014", HttpStatus.CONFLICT, "이미 상담원이 배정되었습니다"),

    // Inquiry Errors
    DUPLICATE_INQUIRY("CHAT_011", HttpStatus.CONFLICT, "해당 공간에 이미 문의 채팅방이 존재합니다"),

    // Internal Errors
    INTERNAL_SERVER_ERROR("CHAT_500", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
