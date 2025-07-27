package jp.co.technopro.talon.util;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DbUtilTest {

    private static Connection conn;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE TK_MEMBER (" +
                    "TK_NO VARCHAR(10), " +
                    "NAME VARCHAR(100), " +
                    "DELETE_FLG CHAR(1))");

            stmt.execute("INSERT INTO TK_MEMBER (TK_NO, NAME, DELETE_FLG) VALUES ('001', '田中太郎', NULL)");
            stmt.execute("INSERT INTO TK_MEMBER (TK_NO, NAME, DELETE_FLG) VALUES ('002', '佐藤花子', '1')");
        }
    }

    @AfterAll
    static void closeConnection() throws SQLException {
        if (conn != null) conn.close();
    }

    @Test
    void testGetCount_matchWithCondition() throws SQLException {
        int count = DbUtil.getCount(conn, "TK_MEMBER", Map.of("TK_NO", "001"));
        assertEquals(1, count);
    }

    @Test
    void testGetCount_matchWithNullCondition() throws SQLException {
        int count = DbUtil.getCount(conn, "TK_MEMBER", Map.of("DELETE_FLG", null));
        assertEquals(1, count);
    }

    @Test
    void testGetCount_noMatch() throws SQLException {
        int count = DbUtil.getCount(conn, "TK_MEMBER", Map.of("TK_NO", "999"));
        assertEquals(0, count);
    }

    @Test
    void testGetCount_allRows() throws SQLException {
        int count = DbUtil.getCount(conn, "TK_MEMBER", Map.of());
        assertEquals(2, count);
    }
}
