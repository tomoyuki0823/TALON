package jp.co.technopro.talon.logic.Gojo.henko;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;
import jp.co.technopro.talon.util.common.MapCheckUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_TK_NO;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_TK_OBJ;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.*;

public class HenkoInitLogic extends AbstractLogicBase {
    @Override
    protected EventResultDto executeLogic() {
        try {
            return henkoInit(conn, paramDto);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TK_MEMBERのデータを基に、履歴テーブル TK_HENKO2 に変更情報を登録する初期処理。
     * <p>
     * 処理月およびTK_NOを指定し、すでに同一キーの履歴が存在しない場合に限り
     * TK_HENKO2 にレコードを挿入します。
     * </p>
     *
     * @param conn     DBコネクション（autoCommit=false 推奨）
     * @param paramDto パラメータDTO（conditionDataに TK_NO, SHORI_TUKI を含む必要あり）
     * @return 結果Map（success: true/false, message: 結果メッセージ）
     * @throws SQLException SQL実行時のエラー
     */
    private EventResultDto henkoInit(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> conditionDataMap = paramDto.getConditionData();
        String tkNo = (String) conditionDataMap.get(MAP_KEY_TK_NO);
        String shoriTuki = (String) conditionDataMap.get(MAP_KEY_SHORI_TUKI);

        // 入力チェック
        if (MapCheckUtil.isAnyBlank(conditionDataMap, MAP_KEY_TK_NO, MAP_KEY_SHORI_TUKI)) {
            return EventResultDto.error(MSG_NON_SHORI_TUKI);
        }

        // TK_MEMBER 取得（DTOで受け取る）
        TkMemberDto tkMemberDto = setTkMemberDto(conn, tkNo, paramDto);
        if (tkMemberDto == null) {
            return EventResultDto.error(MSG_NON_TK_OBJ);
        }

        // すでに履歴があればスキップ
        if (isCntHenko2(conn, tkNo, shoriTuki, paramDto)) {
            return EventResultDto.ok();
        }

        // 処理月セットして履歴登録


        try {
            conn.setAutoCommit(false);
            tkMemberDto.setShoriTuki(shoriTuki);
            insTkHenko2(conn, paramDto, tkMemberDto);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }

        return EventResultDto.ok();
    }
}
