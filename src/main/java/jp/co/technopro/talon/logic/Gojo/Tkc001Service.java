package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.common.ExecutableLogic;
import jp.co.technopro.talon.util.common.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.consts.Gojo.GojoEventIdConst.SIMEDATA;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TKC001;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.insertSimeData;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.isTkc001Empty;
import static jp.co.technopro.talon.util.common.DbUtil.getCount;
import static jp.co.technopro.talon.util.common.TalonSelectUtil.selectHanyoMapList;

public class Tkc001Service implements ExecutableLogic {


    @Override
    public EventResultDto run(Connection conn, TalonParamDto paramDto) {
        try {
            String eventId = paramDto.getEventId();
            switch (eventId) {
                case SIMEDATA:
                    return setSimeData(conn, paramDto);
                default:
                    return EventResultDto.error("未対応のイベントID: " + eventId);
            }
        } catch (Exception e) {
            return EventResultDto.error("処理中にエラーが発生しました: " + e.getMessage());
        }
    }

    /**
     * TKC001 テーブルにデータが存在しない場合、汎用コード（TK_DVS）を元に締データを登録します。
     *
     * @param conn     DB接続
     * @param paramDto TalonパラメータDTO（条件データに SHORI_TUKI を含む）
     * @return 正常終了時のイベント結果DTO
     * @throws SQLException DB操作中にエラーが発生した場合
     */
    private EventResultDto setSimeData(Connection conn, TalonParamDto paramDto) throws SQLException {
        Map<String, Object> params = paramDto.getConditionData();
        String shoriTuki = (String) params.get(MAP_KEY_SHORI_TUKI);

        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            if (isTkc001Empty(conn, shoriTuki, paramDto)) {
                List<Map<String, Object>> hanyouCodeList = selectHanyoMapList(conn, MAP_KEY_TK_DVS, paramDto);
                insertSimeData(conn, shoriTuki, hanyouCodeList, paramDto);
            }

            conn.commit();
            return EventResultDto.ok();
        } catch (Exception e) {
            conn.rollback();
            throw new SQLException("TKC001 insert error", e);
        } finally {
            conn.setAutoCommit(originalAutoCommit); // 呼出元への影響を避ける
        }
    }

}
