package com.teambind.co.kr.chatdding.application.port.in;

/**
 * 메시지 삭제 UseCase
 */
public interface DeleteMessageUseCase {

    DeleteMessageResult execute(DeleteMessageCommand command);
}
