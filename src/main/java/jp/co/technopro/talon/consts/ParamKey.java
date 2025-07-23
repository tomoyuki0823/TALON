package jp.co.technopro.talon.consts;

/**
 * 業務用 Map のキー定数クラス。
 * paramMap や resultMap などで使用されるキーを定義します。
 */
public final class ParamKey {

    private ParamKey() {} // インスタンス化禁止

    /** 特別会員番号 */
    public static final String MAP_KEY_TK_NO = "TK_NO";

    /** 本人退職区分コード */
    public static final String MAP_KEY_HON_TAISYOKU_CD = "HON_TAISYOKU_CD";

    /** 配偶者退職区分コード */
    public static final String MAP_KEY_HAI_TAISYOKU_CD = "HAI_TAISYOKU_CD";

    /** 処理月 */
    public static final String MAP_KEY_SHORI_TUKI = "SHORI_TUKI";

    /** 会員番号（現職チェック用） */
    public static final String MAP_KEY_NO = "no";

    /** 特別会員業務区分  */
    public static final String MAP_KEY_TK_DVS = "TK_DVS";

    /** 成功フラグ */
    public static final String MAP_KEY_SUCCESS = "success";

    /** メッセージ文字列 */
    public static final String MAP_KEY_MSG = "msg";
}
