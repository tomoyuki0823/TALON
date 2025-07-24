package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.sql.SqlLoader;
import jp.co.technopro.talon.util.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.consts.EventId.*;
import static jp.co.technopro.talon.consts.Messages.*;
import static jp.co.technopro.talon.consts.ParamKey.*;
import static jp.co.technopro.talon.consts.SqlXmlPath.*;
import static jp.co.technopro.talon.consts.TableName.*;
import static jp.co.technopro.talon.util.DbUtil.*;
import static jp.co.technopro.talon.util.LogicUtil.buildResult;


public class ShinkiService implements ExecutableLogic {

    private final SqlLoader sqlLoader = new SqlLoader(SQL_SHINKI);

    @Override
    public Map<String, Object> run(Connection conn, Map<String, Object> params, String eventId) throws SQLException {
        switch (eventId) {
            case GENSHOKU_CHK:
                return chkGensyoku(conn, params);

            case DUPLICATE_GENSHOKU_CHK:
                return chkDuplicate(conn, params);

            case SHINKI_HON_TOUROKU:
                return shinkiSimeRenkei(conn, params);

            default:
                return buildResult(false, "未対応のイベントID: " + eventId);
        }
    }

    /**
     * TK_SHINKI テーブルから指定された処理月のデータを TK_MEMBER に連携します。
     * <p>
     * TK_NO をキーに TK_MEMBER の既存レコードを削除した上で、TK_SHINKI の内容を挿入します。<br>
     * 各レコードの挿入処理はトランザクション内で行われ、失敗時はロールバックされます。
     * </p>
     *
     * @param conn   DBコネクション（autoCommit=false 推奨）
     * @param params パラメータ（MAP_KEY_SHORI_TUKI を含む必要あり）
     * @return 結果Map（success=true/false、messageあり）
     * @throws SQLException DBアクセス時のエラー
     */
    private Map<String, Object> shinkiSimeRenkei(Connection conn, Map<String, Object> params) throws SQLException {

        String shoriTuki = (String) params.get(MAP_KEY_SHORI_TUKI);
        if (shoriTuki == null || shoriTuki.isBlank()) {
            return buildResult(false, "処理月（SHORI_TUKI）が指定されていません。");
        }

        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_SHORI_TUKI, shoriTuki);

        List<Map<String, Object>> shinkiList = selectList(conn, TABLE_TK_SHINKI, whereMap);

        if (shinkiList.isEmpty()) {
            return buildResult(true, "処理対象データは存在しません。");
        }

        try {
            for (Map<String, Object> record : shinkiList) {
                String tkNo = (String) record.get(MAP_KEY_TK_NO);

                // 既存レコード削除
                deleteTkMember(conn, tkNo);

                // 新規挿入（false=PK重複時は例外スロー）
                insertByMapEx(conn, TABLE_TK_MEMBER, record, false);
            }

            return buildResult(true, "処理が正常に完了しました。件数: " + shinkiList.size());

        } catch (SQLException e) {
            conn.rollback(); // 明示的にロールバック（呼び出し元がトランザクション制御する場合不要）
            e.printStackTrace();
            return buildResult(false, "処理中にエラーが発生しました: " + e.getMessage());
        }
    }

    /**
     * TK_MEMBER テーブルから指定された TK_NO を削除します。
     *
     * @param conn  DB接続
     * @param tkNo  対象会員番号
     * @throws SQLException SQL例外が発生した場合
     */
    private void deleteTkMember(Connection conn, String tkNo) throws SQLException {
        DbUtil.delete(conn, "DELETE FROM TK_MEMBER WHERE TK_NO = ?", tkNo);
    }

    /**
     * 指定された会員番号が既に退会済みであるかを確認します。
     *
     * <p>GEN_T_KAIIN テーブルにおいて、NO が一致し GOJYO_TAIKAI_CD が NULL のレコードが
     * 存在しない（つまり退会済み）場合に成功と判断します。</p>
     *
     * @param conn   DB接続
     * @param params パラメータ（MAP_KEY_NO を含む必要あり）
     * @return 成功時は true、退会していない場合は false とエラーメッセージ
     * @throws SQLException DBアクセスエラー
     */
    private Map<String, Object> chkGensyoku(Connection conn, Map<String, Object> params) throws SQLException {

        int no = (int) params.get(MAP_KEY_NO);

        // 退会していないデータが存在するかを確認
        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_NO, no);
        whereMap.put(MAP_GOJYO_TAIKAI_CD, null);  // null指定 → IS NULL 検索

        boolean isTaikai = isTableEmpty(conn, TABLE_GEN_T_KAIIN, whereMap);

        if (!isTaikai) {
            return buildResult(false, "現職会員番号: " + no + " は未退会です。");
        }

        return buildResult(true, "");
    }

    /**
     * TK_SHINKI テーブルに指定された会員番号（no）が既に存在するかをチェックします。
     *
     * @param conn   DBコネクション
     * @param params パラメータマップ（"no" キーを含む必要があります）
     * @return 重複があれば success=false とエラーメッセージ、なければ success=true と正常メッセージを含む結果Map
     * @throws SQLException DBアクセスエラーが発生した場合
     */
    private Map<String, Object> chkDuplicate(Connection conn, Map<String, Object> params) throws SQLException {
        int no = (int) params.get(MAP_KEY_NO);
        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_NO, no);

        boolean duplicate = !isTableEmpty(conn, TABLE_TK_SHINKI, whereMap);

        if (duplicate) {
            return buildResult(false, MSG_DUPLICATE_GENSYOKU);
        } else {
            return buildResult(true, "");
        }
    }

}
