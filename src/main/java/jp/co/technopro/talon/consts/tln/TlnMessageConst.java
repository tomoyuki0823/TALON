package jp.co.technopro.talon.consts.tln;

/**
 * TALONログ出力に使用するメッセージ定数クラス。
 * <p>
 * 主に DTO 生成処理やイベントログにおいて、ログメッセージのプレフィックスとして使用されます。
 */
public final class TlnMessageConst {

    private TlnMessageConst() {
        // インスタンス化禁止
    }

    /** 機能IDログ出力用のプレフィックス（例: "機能ID XXX"） */
    public static final String MSG_FUNC_ID_LOG = "機能ID ";

    /** イベントIDログ出力用のプレフィックス（例: "イベントID XXX"） */
    public static final String MSG_EVENT_ID_LOG = "イベントID ";

    /** DTO生成成功時のログ文言 */
    public static final String MSG_DTO_SUCCESS = "DTO生成完了";

    /** DTO生成成功時のログ文言 */
    public static final String MSG_JAVA_LOGIC_ERROR = "Javaロジックの実行中にエラーが発生しました: ";
}
