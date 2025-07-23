package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.util.DbUtil;
import jp.co.technopro.talon.util.DbUtil.Dialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Tkc001Service implements ExecutableLogic {

    @Override
    public void run(Connection conn, Map<String, Object> params, String eventId) {
        switch (eventId) {
            case "SIMEDATA":
                setSimeData(conn, params);
                break;
            default:
                break;
        }
    }

    private void setSimeData(Connection conn, Map<String, Object> params) {
        String shoriTuki = (String) params.get("SHORI_TUKI");

        try {
            conn.setAutoCommit(false);

            if (isTkc001Empty(conn, shoriTuki)) {
                List<Map<String, Object>> hanyouCodeList = fetchHanyouCodes(conn, "TK_DVS");
                insertSimeData(conn, shoriTuki, hanyouCodeList);
            }

            conn.commit();

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception rollbackEx) {
                // rollback失敗時はログだけ出す想定も可
            }
            throw new RuntimeException("TKC001 insert error", e);
        }
    }

    private boolean isTkc001Empty(Connection conn, String shoriTuki) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM TKC001 WHERE SHORI_TUKI = ?";
        return DbUtil.select(conn, sql, shoriTuki)
                .stream().findFirst()
                .map(row -> ((Number) row.get("cnt")).intValue() == 0)
                .orElse(true);
    }

    private List<Map<String, Object>> fetchHanyouCodes(Connection conn, String sikibetuCode) throws SQLException {
        String sql = "SELECT * FROM TLN_M_HANYO_CODE WHERE SIKIBETU_CODE = ?";
        return DbUtil.select(conn, sql, sikibetuCode);
    }

    private void insertSimeData(Connection conn, String shoriTuki, List<Map<String, Object>> hanyouCodeList) throws SQLException {
        for (Map<String, Object> code : hanyouCodeList) {
            Map<String, Object> insMap = new HashMap<>();
            insMap.put("SHORI_TUKI", shoriTuki);
            insMap.put("TK_DVS", code.get("KEY_CODE"));
            insMap.put("SIME_STATUS", "1");

            DbUtil.insertByMap(conn, "TKC001", insMap,
                    Arrays.asList("SHORI_TUKI", "TK_DVS", "SIME_STATUS"),
                    Dialect.SQLSERVER);
        }
    }
}
