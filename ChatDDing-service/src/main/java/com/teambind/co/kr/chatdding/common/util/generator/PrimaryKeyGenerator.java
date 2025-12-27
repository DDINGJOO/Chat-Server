package com.teambind.co.kr.chatdding.common.util.generator;

/**
 * Primary Key Generator Interface
 */
public interface PrimaryKeyGenerator {

    Long generateLongKey();

    @Deprecated(since = "1.1", forRemoval = false)
    String generateKey();
}
