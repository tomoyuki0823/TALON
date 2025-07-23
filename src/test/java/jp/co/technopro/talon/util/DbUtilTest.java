
package jp.co.technopro.talon.util;

import jp.co.technopro.talon.db.DbConfigLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DbUtilTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // 自動的に config/db.properties から接続取得
        conn = DbUtil.getConnectionIfNull(null);

        try (Statement stmt = conn.createStatement()) {
            // テスト用テーブルの初期化
            stmt.execute("IF OBJECT_ID('test_table', 'U') IS NOT NULL DROP TABLE test_table");
            stmt.execute("CREATE TABLE test_table (id INT PRIMARY KEY, name NVARCHAR(50))");
            stmt.execute("INSERT INTO test_table (id, name) VALUES (1, N'Alice'), (2, N'Bob')");
        }
    }

    @Test
    void testSelectOneRowAsMap() throws Exception {
        Map<String, Object> row = DbUtil.selectOneRowAsMap(conn, "SELECT * FROM test_table WHERE id = ?", 1);
        assertNotNull(row);
        assertEquals("Alice", row.get("name"));
    }

    @Test
    void testInsertUpdateDeleteByMap() throws Exception {
        Map<String, Object> row = new HashMap<>();
        row.put("id", 3);
        row.put("name", "Charlie");

        int inserted = DbUtil.insertByMap(conn, "test_table", row, Arrays.asList("id", "name"), DbUtil.Dialect.SQLSERVER);
        assertEquals(1, inserted);

        row.put("name", "Chuck");
        int updated = DbUtil.updateByMap(conn, "test_table", row, Arrays.asList("name"), Arrays.asList("id"), DbUtil.Dialect.SQLSERVER);
        assertEquals(1, updated);

        //int deleted = DbUtil.deleteByMap(conn, "test_table", row, Arrays.asList("id"), DbUtil.Dialect.SQLSERVER);
        //assertEquals(1, deleted);
    }
}
