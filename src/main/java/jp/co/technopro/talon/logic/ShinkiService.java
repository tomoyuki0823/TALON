package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.sql.SqlLoader;

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

            default:
                return buildResult(false, "未対応のイベントID: " + eventId);
        }
    }

    private Map<String, Object> chkGensyoku(Connection conn, Map<String, Object> params) {


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
