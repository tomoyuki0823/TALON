package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.TalonParamDto;
import jp.co.technopro.talon.logic.ExecutableLogic;
import jp.co.technopro.talon.util.DbUtil;
import jp.co.technopro.talon.util.MapCheckUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.EventId.*;
import static jp.co.technopro.talon.consts.Messages.MSG_NON_TK_NO;
import static jp.co.technopro.talon.consts.Messages.MSG_SUCCESS;
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
            case "HENKO_INIT":
                return henkoInit(conn, paramDto);

            default:
                return buildResult(false, "未対応のイベントID: " + eventId);
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
     * @param conn      DBコネクション（autoCommit=falseが推奨される）
     * @param paramDto  パラメータDTO（conditionDataにSHORI_TUKIを含む必要あり）
     * @return 処理結果Map（success: true/false, message: 処理メッセージ）
     * @throws SQLException SQL実行時に発生した例外（トランザクション制御は呼び出し元で行う想定）
     */
    public Map<String, Object> henkoSimeRenkei(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> params = paramDto.getConditionData();

        String shoriTuki = (String) params.get(MAP_KEY_SHORI_TUKI);
        if (shoriTuki == null || shoriTuki.isBlank()) {
            return buildResult(false, "処理月（SHORI_TUKI）が指定されていません。");
        }

        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_SHORI_TUKI, shoriTuki);

        // 対象月の変更データを取得
        List<Map<String, Object>> henkoMapList = selectList(conn, TABLE_TK_HENKO, whereMap);

        if (henkoMapList.isEmpty()) {
            return buildResult(true, "処理対象データは存在しません。");
        }

        try {
            for (Map<String, Object> record : henkoMapList) {
                String tkNo = String.valueOf(record.get(MAP_KEY_TK_NO));

                Map<String, Object> whereMap2 = new HashMap<>();
                whereMap2.put(MAP_KEY_TK_NO, tkNo);

                // TK_NOをキーにレコード更新（該当がなければINSERT可能オプション true）
                updateByMapEx(conn, TABLE_TK_MEMBER, record, whereMap2, true);
            }

            return buildResult(true, "変更締め連携が完了しました。");

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
     * @param conn      DBコネクション（autoCommit=false 推奨）
     * @param paramDto  パラメータDTO（conditionDataに TK_NO, SHORI_TUKI を含む必要あり）
     * @return 結果Map（success: true/false, message: 結果メッセージ）
     * @throws SQLException SQL実行時のエラー
     */
    public Map<String, Object> henkoInit(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> conditionDataMap = paramDto.getConditionData();
        String tkNo = (String) conditionDataMap.get(MAP_KEY_TK_NO);
        String shoriTuki = (String) conditionDataMap.get(MAP_KEY_SHORI_TUKI);

        // 入力チェック
        if (MapCheckUtil.isAnyBlank(conditionDataMap, MAP_KEY_TK_NO, MAP_KEY_SHORI_TUKI)) {
            return buildResult(false, "TK_NOまたは処理月が未入力です。");
        }

        // TK_MEMBER 取得
        Map<String, Object> memberMap = selectOne(conn, TABLE_TK_MEMBER, null, Map.of(MAP_KEY_TK_NO, tkNo), null);
        if (MapCheckUtil.isEmpty(memberMap)) {
            return buildResult(false, MSG_NON_TK_NO);
        }

        // すでに履歴があればスキップ
        if (isCntHenko(conn, tkNo, shoriTuki)) {
            return buildResult(true, "既に登録済みです。");
        }

        // 処理月セットして履歴登録
        memberMap.put(MAP_KEY_SHORI_TUKI, shoriTuki);

        try {
            conn.setAutoCommit(false);
            insertByMapEx(conn, TABLE_TK_HENKO2, memberMap, true);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }

        return buildResult(true, MSG_SUCCESS);
    }

    /**
     * TK_HENKO2 テーブルに対象の履歴データ（TK_NO + SHORI_TUKI）が存在するかをチェックします。
     *
     * @param conn      DBコネクション
     * @param tkNo      対象の会員番号
     * @param shoriTuki 処理月
     * @return true: すでに存在している（＝履歴あり）, false: 未登録
     * @throws SQLException SQL実行時の例外
     */
    private boolean isCntHenko(Connection conn, String tkNo, String shoriTuki) throws SQLException {
        Map<String, Object> whereMap = Map.of(
                MAP_KEY_TK_NO, tkNo,
                MAP_KEY_SHORI_TUKI, shoriTuki
        );

        return DbUtil.getCount(conn, TABLE_TK_HENKO2, whereMap) > 0;
    }
}

