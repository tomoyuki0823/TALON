package jp.co.technopro.talon.consts;

/**
 * 業務メッセージ定数クラス。
 */
public final class Messages {

    private Messages() {}

    /** 現職重複エラー */
    public static final String MSG_DUPLICATE_GENSYOKU = "現職会員番号が既に登録されています。";

    /** 与託金初期化失敗 */
    public static final String MSG_INIT_YOTAKU_FAILED = "与託情報の初期化に失敗しました。";

    /** 一般エラー */
    public static final String MSG_UNKNOWN_ERROR = "予期しないエラーが発生しました。";

    /** 成功 */
    public static final String MSG_SUCCESS = "正常に完了しました。";
}
