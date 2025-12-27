package com.teambind.co.kr.chatdding.common.exception;

import lombok.Getter;

@Getter
public class ChatException extends RuntimeException {

    private final ErrorCode errorCode;

    private ChatException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private ChatException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static ChatException of(ErrorCode errorCode) {
        return new ChatException(errorCode);
    }

    public static ChatException of(ErrorCode errorCode, String message) {
        return new ChatException(errorCode, message);
    }
}
