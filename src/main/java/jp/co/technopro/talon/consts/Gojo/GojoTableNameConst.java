package jp.co.technopro.talon.consts.Gojo;

/**
 * テーブル名定数クラス。
 * SQL発行や条件判定で使用されるテーブル名を定義します。
 */
public final class GojoTableNameConst {

    private GojoTableNameConst() {} // インスタンス化禁止

    /** 預託金予定テーブル */
    public static final String TABLE_TK_T_YOTEKUKIN_YOTEI = "TK_T_YOTEKUKIN_YOTEI";

    /** 預託金マスタ定義テーブル */
    public static final String TABLE_TK_M_YOTEKUKIN_YOTEI = "TK_M_YOTEKUKIN_YOTEI";

    /** 与託情報本体（TK_YOTAKU） */
    public static final String TABLE_TK_YOTAKU = "TK_YOTAKU";

    /** 会員基本情報テーブル */
    public static final String TABLE_TK_MEMBER = "TK_MEMBER";

    /** 支払情報テーブル */
    public static final String TABLE_TK_SHIHARAI = "TK_SHIHARAI";

    /** 締めデータ管理テーブル（TKC001） */
    public static final String TABLE_TKC001 = "TKC001";

    /** 新規加入テーブル */
    public static final String TABLE_TK_SHINKI = "TK_SHINKI";

    /** 新規加入（現職）テーブル */
    public static final String TABLE_GEN_T_KAIIN = "GEN_T_KAIIN";

    /** 変更管理 */
    public static final String TABLE_TK_HENKO = "TK_HENKO";

    /** 変更管理2 */
    public static final String TABLE_TK_HENKO2 = "TK_HENKO2";

    /** 医療費 */
    public static final String TABLE_TK_IRYO = "TK_IRYO";

    /** 銀行マスタ */
    public static final String TABLE_COM_M_BANK = "COM_M_BANK";
}
