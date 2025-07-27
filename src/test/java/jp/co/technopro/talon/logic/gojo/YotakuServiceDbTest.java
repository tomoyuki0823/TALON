package jp.co.technopro.talon.logic.gojo;

import jp.co.technopro.talon.db.DbConfig;
import jp.co.technopro.talon.db.DbConfigLoader;
import jp.co.technopro.talon.dto.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.YotakuService;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YotakuServiceDbTest {

    @Test
    void testSetYotakukinYotei_realDb() throws Exception {
        // === DB設定読み込み ===
        URL resource = getClass().getClassLoader().getResource("config/db.properties");
        assertNotNull(resource, "プロパティファイルが見つかりません");

        DbConfig dbConfig = DbConfigLoader.load();
        assertNotNull(dbConfig);

        try (Connection conn = dbConfig.getConnection()) {
            assertNotNull(conn, "DB接続に失敗しました");

            // === パラメータ準備 ===
            Map<String, Object> conditionMap = new HashMap<>();
            conditionMap.put("TK_NO", "10967"); // テスト用のTK_NOを指定してください

            TalonParamDto paramDto = new TalonParamDto();
            paramDto.setConditionData(conditionMap);

            // === サービス呼び出し ===
            YotakuService service = new YotakuService();

            assertDoesNotThrow(() -> service.setYotakukinYotei(conn, paramDto));

            System.out.println("setYotakukinYotei executed successfully");
        }
    }
}
