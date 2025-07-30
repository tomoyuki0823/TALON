package jp.co.technopro.talon.consts.Gojo;

/**
 * SQL識別子（sql/xml定義ファイル内の select/update タグのID）定数クラス。
 * SqlLoaderなどからSQLを取得する際に使用します。
 */
public final class GojoSqlKeyConst {

    private GojoSqlKeyConst() {} // インスタンス化禁止

    /** 預託金計算用SELECT文（TK_T_YOTEKUKIN_YOTEI + TK_M_YOTEKUKIN_YOTEI結合） */
    public static final String SQL_KEY_CALC_YOTAKUKIN = "CALC_YOTAKUKIN";

    /** 預託金更新処理（TK_YOTAKUのUPDATE） */
    public static final String SQL_KEY_UPDATE_YOTAKU = "UPDATE_YOTAKU";

    /** 預託金更新処理（TK_YOTAKUのUPDATE） */
    public static final String SQL_KEY_UPDATE_TKC001 = "UPDATE_TKC001";

    /** 預託金更新処理（TK_YOTAKUのUPDATE） */
    public static final String SQL_TK_MEMBER = "TK_MEMBER";

    /** 預託金更新処理（TK_YOTAKUのUPDATE） */
    public static final String SQL_TK_SHIHARAI = "TK_SHIHARAI";

}
