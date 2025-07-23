package jp.co.technopro.talon.consts;

/**
 * 業務ロジック識別子（Logic ID）定数クラス。
 * {@link jp.co.technopro.talon.logic.LogicInvoker} などで使用される識別キーを定義します。
 */
public final class LogicId {

    private LogicId() {} // インスタンス化禁止

    /** 医療系処理（IryoService） */
    public static final String IRYO = "IRYO";

    /** 与託金処理（YotakuService） */
    public static final String YOTAKU = "YOTAKU";

    /** 共通処理（TKC001） */
    public static final String COMMON = "COMMON";

    /** 新規加入処理（ShinkiService） */
    public static final String SHINKI = "SHINKI";

    // 今後の追加候補：
    // public static final String SHIHARAI = "SHIHARAI";
    // public static final String SIME = "SIME";
}
