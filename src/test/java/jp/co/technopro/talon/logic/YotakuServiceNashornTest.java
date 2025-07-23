package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.util.DbUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.sql.Connection;
import java.util.*;

import static org.mockito.Mockito.*;

class YotakuServiceNashornTest {

    @Test
    void testYotakuServiceViaNashorn() throws Exception {
        // モック用データ
        Connection mockConn = mock(Connection.class);
        Map<String, Object> tkMember = new HashMap<>();
        tkMember.put("HON_TAISYOKU_CD", "90");
        tkMember.put("HAI_TAISYOKU_CD", "99");
        tkMember.put("HON_YOTAKUKIN", 10000);
        tkMember.put("HAI_YOTAKUKIN", 8000);

        Map<String, Object> tkShiharai = new HashMap<>();
        tkShiharai.put("HON_YOTAKUKIN", 2000);
        tkShiharai.put("HAI_YOTAKUKIN", 3000);

        List<Map<String, Object>> mstList = new ArrayList<>();
        Map<String, Object> m1 = new HashMap<>();
        m1.put("PTN_CD", "1");
        mstList.add(m1);

        // static モック
        try (MockedStatic<DbUtil> mocked = mockStatic(DbUtil.class)) {
            mocked.when(() -> DbUtil.insertByMapAutoCols(any(), any(), any(), any())).thenReturn(1);
            mocked.when(() -> DbUtil.selectOneRowAsMap(eq(mockConn), contains("TK_MEMBER"), any())).thenReturn(tkMember);
            mocked.when(() -> DbUtil.selectOneRowAsMap(eq(mockConn), contains("TK_SHIHARAI"), any())).thenReturn(tkShiharai);
            mocked.when(() -> DbUtil.selectListAsMap(eq(mockConn), contains("TK_M_YOTEKUKIN_YOTEI"), any())).thenReturn(mstList);
            mocked.when(() -> DbUtil.delete(eq(mockConn), contains("DELETE"), any())).thenReturn(1);

            // Nashornスクリプト実行
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.put("conn", mockConn); // Java側から conn をスクリプトに渡す
            engine.eval(new FileReader("src/test/resources/scripts/testYotaku.js"));
        }
    }
}
