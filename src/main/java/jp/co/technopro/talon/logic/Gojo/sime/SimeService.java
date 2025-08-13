package jp.co.technopro.talon.logic.Gojo.sime;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;
import jp.co.technopro.talon.logic.common.ExecutableLogic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.consts.Gojo.GojoEventIdConst.SIMEDATA;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.insertSimeData;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.isTkc001Empty;
import static jp.co.technopro.talon.util.common.TalonSelectUtil.selectHanyoMapList;

public class SimeService extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {
        logInfoClassStart(getClass().getSimpleName());
        return setSimeData();
    }

    /**
     * TKC001 テーブルにデータが存在しない場合、汎用コード（TK_DVS）を元に締データを登録します。
     *
     * @return 正常終了時のイベント結果DTO
     * @throws SQLException DB操作中にエラーが発生した場合
     */
    private EventResultDto setSimeData()  {

        String shoriTuki = getShoriTuki();

        boolean originalAutoCommit = false;
        try {
            originalAutoCommit = conn.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            conn.setAutoCommit(false);

            if (isTkc001Empty(conn, shoriTuki, paramDto)) {
                List<Map<String, Object>> hanyouCodeList = selectHanyoMapList(conn, MAP_KEY_TK_DVS, paramDto);
                insertSimeData(conn, shoriTuki, hanyouCodeList, paramDto);
            }

            conn.commit();
            return EventResultDto.ok();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            try {
                throw new SQLException("TKC001 insert error", e);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit); // 呼出元への影響を避ける
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
