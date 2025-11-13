package jp.co.technopro.talon.consts.Gojo;

/**
 * 業務メッセージ定数クラス。
 */
public final class GojoMessagesConst {

    private GojoMessagesConst() {}

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

    /** データが存在しない */
    public static final String MSG_NON_DATA = "処理対象データは存在しません。";

    /**
     * TODO そもそも、以下って長寿祝金限定？
     * 限定ではないと思うので、JavaDocの修正を行ってください。
     */
    /** 検索時にNULLチェック **/
    public static final String MSG_ERROR_EMPTY = "文字列が空です";

    /** 検索時に文字列チェック **/
    public static final String MSG_ERROR_INVALID = "は有効な年月ではありません";

    /** クエリの実行に失敗したとき **/
    public static final String MSG_ERROR_FAILED = "クエリの実行に失敗しました。\n";

    /** 既に処理済みの時 **/
    public static final String MSG_OK_ALREADY = "既に保存されているため、処理を中止します";
}
