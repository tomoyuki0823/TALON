package jp.co.technopro.talon.logic.gojo;

import jp.co.technopro.talon.db.DbConfig;
import jp.co.technopro.talon.db.DbConfigLoader;
import jp.co.technopro.talon.logic.EventLogicExecutor;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventLogicExecutorTest {

    @Test
    void testExecuteEventLogic_realDb() throws Exception {
        // === DB設定読み込み ===
        URL resource = getClass().getClassLoader().getResource("db.properties");
        assertNotNull(resource, "プロパティファイルが見つかりません");

        DbConfig dbConfig = DbConfigLoader.load();
        assertNotNull(dbConfig, "DB構成の読み込みに失敗しました");

        try (Connection conn = dbConfig.getConnection()) {
            assertNotNull(conn, "DB接続に失敗しました");

            // === パラメータ準備 ===
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("FUNC_ID", "TK_YOTAKU_01");     // 実際の定義に応じて調整
            paramMap.put("EVENT_ID", "SEARCH_BEFORE");   // 実際の定義に応じて調整
            paramMap.put("TK_NO", "10967");             // テスト対象のTK_NOを設定

            Map<String, Object> conditionMap = new HashMap<>();
            conditionMap.put("TK_NO", "10967");
            conditionMap.put("SHORI_TUKI", "202507");

            paramMap.put("CONDITION_DATA", conditionMap);

            // === 実行テスト ===
            Map<String, Object> result = EventLogicExecutor.executeEventLogic(conn, paramMap);

            assertTrue((Boolean) result.get("status"), "ロジックの実行に失敗しました");
            System.out.println("result: " + result);
        }
    }
}
