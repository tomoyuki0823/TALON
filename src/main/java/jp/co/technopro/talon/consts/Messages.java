package jp.co.technopro.talon.consts;

/**
 * 業務メッセージ定数クラス。
 */
public final class Messages {

    private Messages() {}

    /** 現職重複エラー */
    public static final String MSG_DUPLICATE_GENSYOKU = "現職会員番号が既に登録されています。";

    /** 与託金初期化失敗 */
    public static final String MSG_INIT_YOTAKU_FAILED = "預託情報の初期化に失敗しました。";

    /** 一般エラー */
    public static final String MSG_UNKNOWN_ERROR = "予期しないエラーが発生しました。";

    /** 成功 */
    public static final String MSG_SUCCESS = "正常に完了しました。";

    /** 特別会員番号が未設定 */
    public static final String MSG_NON_TK_NO = "特別会員番号が設定されていません、システム管理者に問い合わせてください。";

    /** 特別オブジェクト生成失敗 */
    public static final String MSG_NON_TK_OBJ = "特別会員オブジェクトの生成に失敗しました。システム管理者に問い合わせてください。";

    /** 処理年月が未設定 */
    public static final String MSG_NON_SHORI_TUKI = "処理月（SHORI_TUKI）が指定されていません。";
}
