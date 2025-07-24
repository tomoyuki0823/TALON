package jp.co.technopro.talon.consts;

/**
 * 業務イベントID定数クラス。
 * 各Serviceで使用されるイベント識別子を定義します。
 */
public final class EventId {

    private EventId() {} // インスタンス化禁止

    /** 預託金再計算処理 */
    public static final String CALC_YOTAKUKIN = "CALC_YOTAKUKIN";

    /** 初期登録処理（TK_YOTAKU） */
    public static final String INIT_INFO = "INIT_INFO";

    /** 与託金予定データ作成処理 */
    public static final String YOTAKU_YOTEI = "YOTAKU_YOTEI";

    /** 締めデータ作成処理（TKC001） */
    public static final String SIMEDATA = "SIMEDATA";

    /** 現職チェック（存在確認） */
    public static final String GENSHOKU_CHK = "GENSYOKU_CHK";

    /** 現職重複チェック（TK_SHINKI） */
    public static final String DUPLICATE_GENSHOKU_CHK = "DUPLICATE_GENSYOKU_CHK";

    /** 現職重複チェック（TK_SHINKI） */
    public static final String SHINKI_HON_TOUROKU = "SHINKI_HON_TOUROKU";
}
