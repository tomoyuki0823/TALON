package jp.co.technopro.talon.db;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DbConfigLoaderTest {

    @Test
    void testLoadFromPropertiesFile() {
        // テスト用プロパティファイルのパスを取得
        URL resource = getClass().getClassLoader().getResource("config/db.properties");
        assertNotNull(resource, "プロパティファイルが見つかりません");

        // 実行
        DbConfig config = DbConfigLoader.load();

        // 検証
        assertEquals("jdbc:sqlserver://172.31.6.72:1433;DatabaseName=TALON;encrypt=false;useSSL=false", config.getUrl());
        assertEquals("sa", config.getUser());
        assertEquals("knight", config.getPassword());
        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", config.getDriver());
    }
}
