package jp.co.technopro.talon.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static jp.co.technopro.talon.consts.ParamKey.MAP_KEY_NO;
import static jp.co.technopro.talon.consts.TableName.*;
import static jp.co.technopro.talon.util.DbUtil.isTableEmpty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShinkiServiceTest {

    private ShinkiService service;
    private Connection mockConn;

    @BeforeEach
    void setup() {
        service = new ShinkiService();
        mockConn = mock(Connection.class);
    }

    @Test
    void testChkGensyoku_whenTaikai済み_shouldReturnSuccessTrue() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(MAP_KEY_NO, 123);

        try (MockedStatic<jp.co.technopro.talon.util.DbUtil> dbUtil = mockStatic(jp.co.technopro.talon.util.DbUtil.class)) {
            dbUtil.when(() -> isTableEmpty(eq(mockConn), eq(TABLE_GEN_T_KAIIN), anyMap()))
                    .thenReturn(true); // レコードなし → 退会済み

            Map<String, Object> result = service.run(mockConn, params, "GENSHOKU_CHK");

            assertTrue((Boolean) result.get("success"));
        }
    }

    @Test
    void testChkGensyoku_when未退会_shouldReturnErrorMessage() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(MAP_KEY_NO, 357344);

        try (MockedStatic<jp.co.technopro.talon.util.DbUtil> dbUtil = mockStatic(jp.co.technopro.talon.util.DbUtil.class)) {
            dbUtil.when(() -> isTableEmpty(eq(mockConn), eq(TABLE_GEN_T_KAIIN), anyMap()))
                    .thenReturn(false); // レコードあり → 未退会

            Map<String, Object> result = service.run(mockConn, params, "GENSHOKU_CHK");

            assertFalse((Boolean) result.get("success"));
            //assertTrue(((String) result.get("message")).contains("未退会"));
        }
    }

    @Test
    void testChkDuplicate_when重複なし_shouldReturnSuccessTrue() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(MAP_KEY_NO, 357344);

        try (MockedStatic<jp.co.technopro.talon.util.DbUtil> dbUtil = mockStatic(jp.co.technopro.talon.util.DbUtil.class)) {
            dbUtil.when(() -> isTableEmpty(eq(mockConn), eq(TABLE_TK_SHINKI), anyMap()))
                    .thenReturn(true); // 重複なし

            Map<String, Object> result = service.run(mockConn, params, "DUPLICATE_GENSHOKU_CHK");

            assertTrue((Boolean) result.get("success"));
        }
    }

    @Test
    void testChkDuplicate_when重複あり_shouldReturnError() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(MAP_KEY_NO, 999);

        try (MockedStatic<jp.co.technopro.talon.util.DbUtil> dbUtil = mockStatic(jp.co.technopro.talon.util.DbUtil.class)) {
            dbUtil.when(() -> isTableEmpty(eq(mockConn), eq(TABLE_TK_SHINKI), anyMap()))
                    .thenReturn(false); // 重複あり

            Map<String, Object> result = service.run(mockConn, params, "DUPLICATE_GENSHOKU_CHK");

            assertFalse((Boolean) result.get("success"));
            // assertTrue(((String) result.get("message")).contains("重複"));
        }
    }

    @Test
    void testRun_withUnsupportedEvent_shouldReturnError() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(MAP_KEY_NO, 1);

        Map<String, Object> result = service.run(mockConn, params, "UNSUPPORTED");

        assertFalse((Boolean) result.get("success"));
        // assertTrue(((String) result.get("message")).contains("未対応"));
    }
}
