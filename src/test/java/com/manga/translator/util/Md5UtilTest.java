package com.manga.translator.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Md5Util 单元测试。
 */
class Md5UtilTest {

    @Test
    void shouldReturnMd5Hash_when_inputIsValid() {
        String result = Md5Util.md5("hello");
        assertEquals("5d41402abc4b2a76b9719d911017c592", result);
    }

    @Test
    void shouldReturnMd5Hash_when_inputContainsChinese() {
        String result = Md5Util.md5("你好");
        assertNotNull(result);
        assertEquals(32, result.length());
    }

    @Test
    void shouldReturnMd5Hash_when_inputIsEmpty() {
        String result = Md5Util.md5("");
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", result);
    }

    @Test
    void shouldProduceConsistentHash_when_sameInput() {
        String input = "test123!@#";
        assertEquals(Md5Util.md5(input), Md5Util.md5(input));
    }

    @Test
    void shouldProduceDifferentHash_when_differentInput() {
        assertNotEquals(Md5Util.md5("abc"), Md5Util.md5("abd"));
    }
}
