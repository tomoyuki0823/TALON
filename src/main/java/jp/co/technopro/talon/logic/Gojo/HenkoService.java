package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.common.ExecutableLogic;
import jp.co.technopro.talon.util.common.MapCheckUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoEventIdConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_TK_NO;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.*;

public class HenkoService implements ExecutableLogic {

    public EventResultDto run(Connection conn, TalonParamDto paramDto) throws Exception {

        String eventId = paramDto.getEventId();
        switch (eventId) {

            case HENKO_HON_TOUROKU:
                return henkoSimeRenkei(conn, paramDto);
            case "HENKO_INIT":
                return henkoInit(conn, paramDto);

            default:
                return EventResultDto.error("未対応のイベントID: " + eventId);
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
    public EventResultDto henkoSimeRenkei(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> params = paramDto.getConditionData();

        String shoriTuki = (String) params.get(MAP_KEY_SHORI_TUKI);
        if (shoriTuki == null || shoriTuki.isBlank()) {

            return EventResultDto.error(MSG_NON_SHORI_TUKI);
        }

        // 対象月の変更データを取得
        List<Map<String, Object>> henkoMapList = getHenkoReflectTargetList(conn, shoriTuki);

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
    public EventResultDto henkoInit(Connection conn, TalonParamDto paramDto) throws Exception {

        Map<String, Object> conditionDataMap = paramDto.getConditionData();
        String tkNo = (String) conditionDataMap.get(MAP_KEY_TK_NO);
        String shoriTuki = (String) conditionDataMap.get(MAP_KEY_SHORI_TUKI);

        // 入力チェック
        if (MapCheckUtil.isAnyBlank(conditionDataMap, MAP_KEY_TK_NO, MAP_KEY_SHORI_TUKI)) {
            return EventResultDto.error(MSG_NON_SHORI_TUKI);
        }

        // TK_MEMBER 取得（DTOで受け取る）
        TkMemberDto tkMemberDto = setTkMemberDto(conn, tkNo);
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