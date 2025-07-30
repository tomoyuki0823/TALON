package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.common.ExecutableLogic;
import jp.co.technopro.talon.sql.common.SqlLoader;
import jp.co.technopro.talon.util.common.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_SHINKI;
import static jp.co.technopro.talon.consts.Gojo.GojoEventIdConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoSqlXmlPathConst.SQL_GOJO_SHINKI;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.*;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.*;
import static jp.co.technopro.talon.util.Gojo.GojoLogicUtil.getShoriTukiFromConditionData;
import static jp.co.technopro.talon.util.common.DbUtil.*;

public class ShinkiService implements ExecutableLogic {

    @Override
    public EventResultDto run(Connection conn, TalonParamDto paramDto) throws SQLException {
        String eventId = paramDto.getEventId();
        switch (eventId) {
            case SHINKI_GENSHOKU_CHK:
                return chkGensyoku(conn, paramDto);

            case SHINKI_DUPLICATE_GENSHOKU_CHK:
                return chkDuplicate(conn, paramDto);

            case SHINKI_HON_TOUROKU:
                return shinkiSimeRenkei(conn, paramDto);

            default:
                return EventResultDto.error("未対応のイベントID: " + eventId);
        }
    }

    /**
     * TK_SHINKI テーブルから指定された処理月のデータを TK_MEMBER に連携します。
     *
     * @param conn     DBコネクション（autoCommit=false 推奨）
     * @param paramDto 処理対象 DTO（SHORI_TUKI を含む必要あり）
     * @return 処理結果（success=true/false、メッセージ付き）
     * @throws SQLException DBアクセス時のエラー
     */
    public EventResultDto shinkiSimeRenkei(Connection conn, TalonParamDto paramDto) throws SQLException {
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

    /**
     * 指定された会員番号が既に退会済みであるかを確認します。
     *
     * <p>GEN_T_KAIIN テーブルにおいて、NO が一致し GOJYO_TAIKAI_CD が NULL のレコードが
     * 存在しない（つまり退会済み）場合に成功と判断します。</p>
     *
     * @param conn     DB接続
     * @param paramDto パラメータ（MAP_KEY_NO を含む必要あり）
     * @return 成功時は true、退会していない場合は false とエラーメッセージ
     * @throws SQLException DBアクセスエラー
     */
    public EventResultDto chkGensyoku(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> params = paramDto.getTargetData();

        Object noObj = params.get(MAP_KEY_NO);
        if (noObj == null) {
            return EventResultDto.error("会員番号（NO）が指定されていません。");
        }

        int no;
        try {
            no = Integer.parseInt(noObj.toString());
        } catch (NumberFormatException e) {
            return EventResultDto.error("会員番号（NO）が数値でありません。");
        }

        // 退会していないデータが存在するかを確認
        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_NO, no);
        whereMap.put(MAP_GOJYO_TAIKAI_CD, null);  // null指定 → IS NULL 検索

        boolean isTaikai = isTableEmpty(conn, TABLE_GEN_T_KAIIN, whereMap, paramDto.getCompanyCode());

        if (!isTaikai) {
            return EventResultDto.error("現職会員番号: " + no + " は未退会です。");
        }

        return EventResultDto.ok();
    }

    /**
     * TK_SHINKI テーブルに指定された会員番号（no）が既に存在するかをチェックします。
     *
     * @param conn     DBコネクション
     * @param paramDto パラメータマップ（"no" キーを含む必要があります）
     * @return 重複があれば success=false とエラーメッセージ、なければ success=true と正常メッセージを含む結果Map
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    public EventResultDto chkDuplicate(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> params = paramDto.getTargetData();
        Object noObj = params.get(MAP_KEY_NO);
        if (noObj == null) {

            return EventResultDto.error("会員番号（NO）が指定されていません。");
        }

        int no;
        try {
            no = Integer.parseInt(noObj.toString());
        } catch (NumberFormatException e) {
            return EventResultDto.error("会員番号（NO）が数値でありません。");
        }
        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_NO, no);

        boolean duplicate = !isTableEmpty(conn, TABLE_TK_SHINKI, whereMap, paramDto.getCompanyCode());

        if (duplicate) {
            return EventResultDto.error(MSG_DUPLICATE_GENSYOKU);

        } else {
            return EventResultDto.ok();
        }
    }

}
