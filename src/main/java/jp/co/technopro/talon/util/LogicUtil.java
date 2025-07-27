package jp.co.technopro.talon.util;

import jp.co.technopro.talon.sql.SqlLoader;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static jp.co.technopro.talon.consts.MapKeyCommon.MAP_KEY_MESSAGE;
import static jp.co.technopro.talon.consts.MapKeyCommon.MAP_KEY_STATUS;
import static jp.co.technopro.talon.consts.SqlKey.SQL_KEY_UPDATE_TKC001;
import static jp.co.technopro.talon.consts.SqlXmlPath.SQL_COMMON;

public class LogicUtil {

    private static final SqlLoader sqlLoader = new SqlLoader(SQL_COMMON);

    /**
     * ロジック共通の返却Mapを構築する（Nashorn側への返却用）
     *
     * @param success 成功フラグ
     * @param msg     メッセージ（成功またはエラー内容）
     * @return Map形式の返却値（"success", "msg" を含む）
     */
    public static Map<String, Object> buildResult(boolean success, String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put(MAP_KEY_STATUS, success);
        result.put(MAP_KEY_MESSAGE, msg);
        return result;
    }

    /**
     * TKC001 テーブルの該当レコードの SIME_STATUS を '2' に更新します。
     *
     * @param conn      DBコネクション
     * @param shoriTuki 処理月（SHORI_TUKI）
     * @param tkDvs     区分（TK_DVS）
     * @throws SQLException SQLエラーが発生した場合
     */
    public static void updateTkc001(Connection conn, String shoriTuki, String tkDvs) throws SQLException {
        String sql = sqlLoader.get(SQL_KEY_UPDATE_TKC001);
        DbUtil.update(conn, sql, shoriTuki, tkDvs);
    }

}
