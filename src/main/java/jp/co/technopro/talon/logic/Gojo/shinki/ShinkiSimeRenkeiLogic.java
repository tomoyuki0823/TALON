package jp.co.technopro.talon.logic.Gojo.shinki;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_SHINKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_SHORI_TUKI;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.*;
import static jp.co.technopro.talon.util.Gojo.GojoLogicUtil.getShoriTukiFromConditionData;

public class ShinkiSimeRenkeiLogic extends GojoAbstractLogicBase {
    @Override
    protected EventResultDto executeLogic() {
        try {
            logInfoClassStart(getClass().getSimpleName());
            return shinkiSimeRenkei();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TK_SHINKI テーブルから指定された処理月のデータを TK_MEMBER に連携します。
     *
     * @return 処理結果（success=true/false、メッセージ付き）
     * @throws SQLException DBアクセス時のエラー
     */
    private EventResultDto shinkiSimeRenkei() throws SQLException {
        String shoriTuki = getShoriTukiFromConditionData(paramDto);
        if (shoriTuki == null) {
            return EventResultDto.error(MSG_NON_SHORI_TUKI);
        }

        List<Map<String, Object>> shinkiList = loadShinkiData(conn, shoriTuki);
        if (shinkiList.isEmpty()) {
            return EventResultDto.error("新規登録の処理対象データは存在しません。");
        }

        try {
            for (Map<String, Object> record : shinkiList) {
                processShinkiRecord(conn, paramDto.getCompanyCode(), record);
            }

            updateTkc001(conn, shoriTuki, TK_DVS_SHINKI);
            return EventResultDto.ok();

        } catch (SQLException e) {
            conn.rollback();
            return EventResultDto.error("処理中にエラーが発生しました: " + e.getMessage());
        }
    }

}