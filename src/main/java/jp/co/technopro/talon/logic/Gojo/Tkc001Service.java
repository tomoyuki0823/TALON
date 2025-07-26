package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.TalonParamDto;
import jp.co.technopro.talon.logic.ExecutableLogic;
import jp.co.technopro.talon.util.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.consts.EventId.SIMEDATA;
import static jp.co.technopro.talon.consts.ParamKey.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.util.LogicUtil.buildResult;

public class Tkc001Service implements ExecutableLogic {

    @Override
    public Map<String, Object> run(Connection conn, TalonParamDto paramDto) {
        try {
            String eventId = paramDto.getEventId();
            switch (eventId) {
                case SIMEDATA:
                    return setSimeData(conn, paramDto);
                default:
                    return buildResult(false, "未対応のイベントID: " + eventId);
            }
        } catch (Exception e) {
            return buildResult(false, "処理中にエラーが発生しました: " + e.getMessage());
        }
    }

    private Map<String, Object> setSimeData(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> params = paramDto.getConditionData();

        String shoriTuki = (String) params.get(MAP_KEY_SHORI_TUKI);

        conn.setAutoCommit(false);
        try {
            if (isTkc001Empty(conn, shoriTuki)) {
                List<Map<String, Object>> hanyouCodeList = fetchHanyouCodes(conn, "TK_DVS");
                insertSimeData(conn, shoriTuki, hanyouCodeList);
            }
            conn.commit();
            return buildResult(true, "締データの登録が完了しました");
        } catch (Exception e) {
            conn.rollback();
            throw new SQLException("TKC001 insert error", e);
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
                    Arrays.asList("SHORI_TUKI", "TK_DVS", "SIME_STATUS"));
        }
    }

}
