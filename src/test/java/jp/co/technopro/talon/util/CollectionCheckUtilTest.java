package jp.co.technopro.talon.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CollectionCheckUtilTest {

    @Test void testIsNotEmpty() {
        assertTrue(CollectionCheckUtil.isNotEmpty(Arrays.asList("a")));
        assertFalse(CollectionCheckUtil.isNotEmpty(Collections.emptyList()));
        assertFalse(CollectionCheckUtil.isNotEmpty((Collection<?>) null));
    }

    @Test void testIsEmptyOrNull() {
        assertTrue(CollectionCheckUtil.isEmptyOrNull((Collection<?>) null));
        assertTrue(CollectionCheckUtil.isEmptyOrNull(Collections.emptySet()));
        assertFalse(CollectionCheckUtil.isEmptyOrNull(List.of("x")));
    }

    @Test void testAnyNull() {
        assertTrue(CollectionCheckUtil.anyNull(Arrays.asList("a", null, "b")));
        assertFalse(CollectionCheckUtil.anyNull(Arrays.asList("a", "b")));
        assertFalse(CollectionCheckUtil.anyNull(null));
    }

    @Test void testContainsOnlyNull() {
        assertTrue(CollectionCheckUtil.containsOnlyNull(Arrays.asList(null, null)));
        assertFalse(CollectionCheckUtil.containsOnlyNull(Arrays.asList(null, "x")));
        assertTrue(CollectionCheckUtil.containsOnlyNull(null));
    }

    @Test void testContainsDuplicates() {
        assertTrue(CollectionCheckUtil.containsDuplicates(Arrays.asList("a", "b", "a")));
        assertFalse(CollectionCheckUtil.containsDuplicates(Arrays.asList("a", "b", "c")));
        assertFalse(CollectionCheckUtil.containsDuplicates(null));
    }

    @Test void testCountNonNull() {
        assertEquals(2, CollectionCheckUtil.countNonNull(Arrays.asList("a", null, "b")));
        assertEquals(0, CollectionCheckUtil.countNonNull(Arrays.asList(null, null)));
        assertEquals(0, CollectionCheckUtil.countNonNull(null));
    }

    @Test void testAllEmptyString() {
        assertTrue(CollectionCheckUtil.allEmptyString(Arrays.asList(null, "", " ")));
        assertFalse(CollectionCheckUtil.allEmptyString(Arrays.asList("a", null)));
        assertTrue(CollectionCheckUtil.allEmptyString(Collections.emptyList()));
        assertFalse(CollectionCheckUtil.allEmptyString(Arrays.asList(1, 2, 3))); // 非String
    }
}
