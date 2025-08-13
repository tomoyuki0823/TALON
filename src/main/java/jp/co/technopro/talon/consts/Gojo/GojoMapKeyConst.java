package jp.co.technopro.talon.consts.Gojo;

/**
 * 業務用 Map のキー定数クラス。
 * paramMap や resultMap などで使用されるキーを定義します。
 */
public final class GojoMapKeyConst {

    private GojoMapKeyConst() {
    } // インスタンス化禁止

    // ==============================
    // 会員情報
    // ==============================

    /** 特別会員番号 */
    public static final String MAP_KEY_TK_NO = "TK_NO";

    /** 本人退職区分コード */
    public static final String MAP_KEY_HON_TAISYOKU_CD = "HON_TAISYOKU_CD";

    /** 配偶者退職区分コード */
    public static final String MAP_KEY_HAI_TAISYOKU_CD = "HAI_TAISYOKU_CD";

    /** 続き柄（01:本人, 02:配偶者） */
    public static final String MAP_KEY_ZOKU = "ZOKU";

    /** 会員番号（現職チェック用） */
    public static final String MAP_KEY_NO = "no";

    /** 会員番号（現職チェック用） */
    public static final String MAP_KEY_GOJYO_TAIKAI_CD = "GOJYO_TAIKAI_CD";

    /** 退職フラグ */
    public static final String MAP_KEY_TAISYOKU_FLG = "TAISYOKU_FLG";

    /** 退職フラグ */
    public static final String MAP_KEY_RYOYO_NENGETU = "RYOYO_NENGETU";

    /** 銀行コード */
    public static final String MAP_KEY_BANK_CD = "BANK_CD";

    /** 支店コード */
    public static final String MAP_KEY_SHITEN_CD = "SHITEN_CD";

    // ==============================
    // 処理・状態系
    // ==============================

    /** 処理月（yyyyMM形式） */
    public static final String MAP_KEY_SHORI_TUKI = "SHORI_TUKI";

    /** 締め状態（0:未締, 1:締済） */
    public static final String MAP_KEY_SIME_STATUS = "SIME_STATUS";

    /** 区分（退会・現職などの判定に使用） */
    public static final String MAP_KEY_TK_DVS = "TK_DVS";

    // ==============================
    // 金額・送金情報
    // ==============================

    /** 預託金金額 */
    public static final String MAP_KEY_YOTAKUKIN = "YOTAKUKIN";

    /** 弔慰金金額 */
    public static final String MAP_KEY_TYOIKIN = "TYOIKIN";


    // ==============================
    // 汎用メッセージ
    // ==============================

    /** メッセージ文字列 */
    public static final String MAP_KEY_MSG = "msg";
}
