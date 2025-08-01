package jp.co.technopro.talon.logic.Gojo.henko;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_SHORI_TUKI;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.getHenkoReflectTargetList;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.upsertMemberRecord;

public class HenkoSimeRenkeiLogic extends AbstractLogicBase {
    @Override
    protected EventResultDto executeLogic() {
        try {
            return henkoSimeRenkei(conn, paramDto);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TK_HENKO テーブルから指定された処理月のデータを取得し、
     * TK_MEMBER テーブルに対して該当レコードを上書き更新します。
     *
     * <p>該当する SHORI_TUKI のデータが存在しない場合は正常終了とし、何も更新しません。</p>
     *
     * <p>主キーである TK_NO をWHERE句に指定し、SHORI_TUKIを除く他のカラムを更新対象として扱います。</p>
     *
     * @param conn     DBコネクション（autoCommit=falseが推奨される）
     * @param paramDto パラメータDTO（conditionDataにSHORI_TUKIを含む必要あり）
     * @return 処理結果Map（success: true/false, message: 処理メッセージ）
     * @throws SQLException SQL実行時に発生した例外（トランザクション制御は呼び出し元で行う想定）
     */
    private EventResultDto henkoSimeRenkei(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> params = paramDto.getConditionData();

        String shoriTuki = (String) params.get(MAP_KEY_SHORI_TUKI);
        if (shoriTuki == null || shoriTuki.isBlank()) {

            return EventResultDto.error(MSG_NON_SHORI_TUKI);
        }

        // 対象月の変更データを取得
        List<Map<String, Object>> henkoMapList = getHenkoReflectTargetList(conn, shoriTuki, paramDto);

        if (henkoMapList.isEmpty()) {
            return EventResultDto.error("処理対象データは存在しません。");

        }

        try {
            for (Map<String, Object> record : henkoMapList) {

                // TK_NOをキーにレコード更新（該当がなければINSERT可能オプション true）
                upsertMemberRecord(conn, record, paramDto);
            }

            return EventResultDto.ok();

        } catch (SQLException ex) {
            // ロールバックは呼び出し元で制御される前提
            throw ex;
        }
    }

}
