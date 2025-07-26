package jp.co.technopro.talon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringCheckUtilTest {

    @Test
    @DisplayName("isNotEmpty: null または空文字は false")
    void testIsNotEmpty() {
        assertFalse(StringCheckUtil.isNotEmpty(null));
        assertFalse(StringCheckUtil.isNotEmpty(""));
        assertTrue(StringCheckUtil.isNotEmpty("a"));
    }

    @Test
    @DisplayName("isNotBlank: null, 空文字, 空白だけは false")
    void testIsNotBlank() {
        assertFalse(StringCheckUtil.isNotBlank(null));
        assertFalse(StringCheckUtil.isNotBlank(""));
        assertFalse(StringCheckUtil.isNotBlank("  "));
        assertTrue(StringCheckUtil.isNotBlank("a"));
        assertTrue(StringCheckUtil.isNotBlank(" a "));
    }

    @Test
    @DisplayName("isNullOrEmpty: null または空文字は true")
    void tesIsNullOrEmpty() {
        assertTrue(StringCheckUtil.isNullOrEmpty(null));
        assertTrue(StringCheckUtil.isNullOrEmpty(""));
        assertFalse(StringCheckUtil.isNullOrEmpty(" "));
        assertFalse(StringCheckUtil.isNullOrEmpty("abc"));
    }

    @Test
    @DisplayName("isBlankOrNull: null, 空文字, 空白だけは true")
    void testIsBlankOrNull() {
        assertTrue(StringCheckUtil.isBlankOrNull(null));
        assertTrue(StringCheckUtil.isBlankOrNull(""));
        assertTrue(StringCheckUtil.isBlankOrNull("   "));
        assertFalse(StringCheckUtil.isBlankOrNull("a"));
    }

    @Test
    @DisplayName("isNumeric: 数字だけは true、他は false")
    void testIsNumeric() {
        assertFalse(StringCheckUtil.isNumeric(null));
        assertFalse(StringCheckUtil.isNumeric(""));
        assertFalse(StringCheckUtil.isNumeric("abc"));
        assertFalse(StringCheckUtil.isNumeric("123abc"));
        assertTrue(StringCheckUtil.isNumeric("123456"));
    }

    @Test
    @DisplayName("isAlpha: 英字だけは true、他は false")
    void testIsAlpha() {
        assertFalse(StringCheckUtil.isAlpha(null));
        assertFalse(StringCheckUtil.isAlpha(""));
        assertFalse(StringCheckUtil.isAlpha("123"));
        assertFalse(StringCheckUtil.isAlpha("abc123"));
        assertTrue(StringCheckUtil.isAlpha("abc"));
        assertTrue(StringCheckUtil.isAlpha("ABC"));
    }
}
