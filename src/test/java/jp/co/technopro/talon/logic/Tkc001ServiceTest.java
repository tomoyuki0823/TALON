
package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.db.DbConfigLoader;
import jp.co.technopro.talon.util.DbUtil;
import jp.co.technopro.talon.util.DbUtil.Dialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class Tkc001ServiceTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        conn = DbUtil.getConnectionIfNull(null);


    }

    @Test
    void testRun_createsRecordsIfNotExists() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("SHORI_TUKI", "202507");

        Tkc001Service service = new Tkc001Service();
        service.run(conn, params, "test_event");

        List<Map<String, Object>> result = DbUtil.select(conn, "SELECT * FROM TKC001 WHERE SHORI_TUKI = ?", "202508");
        assertEquals(7, result.size());

        Set<String> dvsSet = new HashSet<>();
        for (Map<String, Object> row : result) {
            dvsSet.add((String) row.get("TK_DVS"));
            assertEquals("1", row.get("SIME_STATUS"));
        }

    }

    @Test
    void testRun_doesNothingIfExists() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO TKC001 (SHORI_TUKI, TK_DVS, SIME_STATUS) VALUES ('202508', '1', '1')");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("SHORI_TUKI", "202508");

        Tkc001Service service = new Tkc001Service();
        service.run(conn, params, "test_event");

        List<Map<String, Object>> result = DbUtil.select(conn, "SELECT * FROM TKC001 WHERE SHORI_TUKI = ?", "202508");
        assertEquals(1, result.size());  // 既存1件だけ
    }
}
