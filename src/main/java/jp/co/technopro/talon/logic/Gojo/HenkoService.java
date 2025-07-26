package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.TalonParamDto;
import jp.co.technopro.talon.logic.ExecutableLogic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.EventId.*;
import static jp.co.technopro.talon.consts.ParamKey.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.ParamKey.MAP_KEY_TK_NO;
import static jp.co.technopro.talon.consts.TableName.*;
import static jp.co.technopro.talon.util.DbUtil.*;
import static jp.co.technopro.talon.util.LogicUtil.buildResult;

public class HenkoService implements ExecutableLogic {

    public Map<String, Object> run(Connection conn, TalonParamDto paramDto) throws SQLException {

        String eventId = paramDto.getEventId();
        switch (eventId) {

            case HENKO_HON_TOUROKU:
                return henkoSimeRenkei(conn, paramDto);

            default:
                return buildResult(false, "未対応のイベントID: " + eventId);
        }

    }

    public Map<String, Object> henkoSimeRenkei(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> params = paramDto.getConditionData();

        String shoriTuki = (String) params.get(MAP_KEY_SHORI_TUKI);
        if (shoriTuki == null || shoriTuki.isBlank()) {
            return buildResult(false, "処理月（SHORI_TUKI）が指定されていません。");
        }

        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_SHORI_TUKI, shoriTuki);

        List<Map<String, Object>> henkoMapList = selectList(conn, TABLE_TK_HENKO, whereMap);

        if (henkoMapList.isEmpty()) {
            return buildResult(true, "処理対象データは存在しません。");
        }

        try {
            for (Map<String, Object> record : henkoMapList) {
                String tkNo = String.valueOf(record.get(MAP_KEY_TK_NO));

                Map<String, Object> whereMap2 = new HashMap<>();
                whereMap2.put(MAP_KEY_TK_NO, tkNo);

                updateByMapEx(conn, TABLE_TK_MEMBER, record, whereMap2, true);
            }

            return buildResult(true, "変更締め連携が完了しました。");

        } catch (SQLException ex) {
            // ログ出力やロールバック対応は必要に応じて追加
            throw ex;
        }
    }

}

