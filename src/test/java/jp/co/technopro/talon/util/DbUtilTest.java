
package jp.co.technopro.talon.util;

import jp.co.technopro.talon.db.DbConfigLoader;
import jp.co.technopro.talon.util.db.DbDialect;
import jp.co.technopro.talon.util.db.DbDialectFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
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

    @AfterEach
    void tearDown() {
    }

    @Test
    void isTableEmpty() {
    }

    @Test
    void gettableColList() throws SQLException {
        Map<String, Object> dummyMap = new HashMap<>();
        dummyMap.put("TK_NO", "10006");
        dummyMap.put("name", null);
        List<String> result = DbUtil.gettableColList(conn, "TK_MEMBER", dummyMap);
        assertTrue(result.contains("TK_NO"));
        assertFalse(result.contains("name"));
    }

    @Test
    void testGetTableColumns() throws Exception {
        Map<String, Object> dummyMap = new HashMap<>();
        dummyMap.put("id", null);
        dummyMap.put("name", null);
        dummyMap.put("unknown_col", "x"); // 存在しないカラム

        DbDialect dialect = DbDialectFactory.createDialect(conn);
        List<String> result = dialect.getTableColumns(conn, "test_table", dummyMap);

        assertTrue(result.contains("id"));
        assertTrue(result.contains("name"));
        assertFalse(result.contains("unknown_col"));
    }


    @Test
    void testInsertByMapEx_withDialect() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 4);
        data.put("name", "Diana");

        DbDialect dialect = DbDialectFactory.createDialect(conn);
        List<String> cols = dialect.getTableColumns(conn, "test_table", data);

        assertTrue(cols.contains("id"));
        assertTrue(cols.contains("name"));

        // 実行
        DbUtil.insertByMapEx(conn, "test_table", data, true);

        // 検証
        Map<String, Object> row = DbUtil.selectOne(conn, "SELECT * FROM test_table WHERE id = ?", 4);
        assertNotNull(row);
        assertEquals("Diana", row.get("name"));
    }

    @Test
    void testSelectOne_success() throws Exception {
        Map<String, Object> result = DbUtil.selectOne(conn, "SELECT * FROM test_table WHERE name = ?", "Bob");
        assertNotNull(result);
        assertEquals(2, result.get("id"));
    }

}
