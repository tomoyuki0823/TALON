package jp.co.technopro.talon.consts;

/**
 * SQL識別子（sql/xml定義ファイル内の select/update タグのID）定数クラス。
 * SqlLoaderなどからSQLを取得する際に使用します。
 */
public final class SqlKey {

    private SqlKey() {} // インスタンス化禁止

    /** 預託金計算用SELECT文（TK_T_YOTEKUKIN_YOTEI + TK_M_YOTEKUKIN_YOTEI結合） */
    public static final String SQL_KEY_CALC_YOTAKUKIN = "CALC_YOTAKUKIN";

    /** 預託金更新処理（TK_YOTAKUのUPDATE） */
    public static final String SQL_KEY_UPDATE_YOTAKU = "UPDATE_YOTAKU";

    // 今後の追加例：
    // public static final String SQL_KEY_SELECT_MEMBER = "SELECT_MEMBER";
    // public static final String SQL_KEY_INSERT_HISTORY = "INSERT_HISTORY";
}
