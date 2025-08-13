
package jp.co.technopro.talon.util.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DbUtilTest {

    private Connection conn;

    @BeforeEach
    void setup() throws Exception {
        conn = DbTestUtil.getTestConnection();
    }

    @Test
    void testGetNumberingData_insertIfBreakKeyMissing() throws SQLException {
        Map<String, Object> result = DbUtil.getNumberingData(conn, "AUTO_NO", 3);
        assertNotNull(result);
        assertTrue(result.containsKey("NO"));
        assertTrue(result.containsKey("NO_LIST"));

        String no = (String) result.get("NO");
        @SuppressWarnings("unchecked")
        List<String> noList = (List<String>) result.get("NO_LIST");

        assertEquals(3, noList.size());
        assertTrue(no.matches("\\d{6}\\d{4}")); // ← ここが修正点
    }

}
