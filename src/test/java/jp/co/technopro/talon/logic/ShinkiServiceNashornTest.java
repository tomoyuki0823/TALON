package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.util.DbUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.sql.Connection;
import java.util.*;

import static org.mockito.Mockito.*;

class ShinkiServiceNashornTest {

    @Test
    void testShinkiServiceViaNashorn() throws Exception {
        // 1. モック接続とデータ
        Connection mockConn = mock(Connection.class);

        // TK_SHINKIにデータが存在しないことを想定
        Map<String, Object> emptyResult = new HashMap<>();
        emptyResult.put("cnt", 0);

        // 2. DbUtilのstaticメソッドをモック
        try (MockedStatic<DbUtil> mocked = mockStatic(DbUtil.class)) {
            mocked.when(() -> DbUtil.select(any(), contains("SELECT COUNT(*)"), any()))
                    .thenReturn(Collections.singletonList(emptyResult));

            mocked.when(() -> DbUtil.isTableEmpty(eq(mockConn), eq("TK_SHINKI"), any()))
                    .thenReturn(true); // デフォルト動作

            // 3. スクリプト実行
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.put("conn", mockConn); // Javaから渡す接続
            engine.eval(new FileReader("src/test/resources/scripts/testShinki.js"));
        }
    }
}
