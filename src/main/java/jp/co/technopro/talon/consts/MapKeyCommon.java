package jp.co.technopro.talon.consts;

/**
 * イベント処理やロジック実行時に使用される Map の共通キー定数を定義するユーティリティクラス。
 * <p>
 * {@link java.util.Map Map&lt;String, Object&gt;} を介して各種情報をやり取りする際に、
 * 使用するキー名のタイポ防止と共通化を目的とします。
 * </p>
 */
public final class MapKeyCommon {

    private MapKeyCommon() {
        // インスタンス化禁止
    }

    /** 機能IDを表すキー名（例: "FUNC_ID"） */
    public static final String MAP_KEY_FUNC_ID = "FUNC_ID";

    /** イベントIDを表すキー名（例: "EVENT_ID"） */
    public static final String MAP_KEY_EVENT_ID = "EVENT_ID";

    /** ロジックIDを表すキー名（例: "LOGIC_ID"） */
    public static final String MAP_KEY_LOGIC_ID = "LOGIC_ID";

    /** 有効フラグを表すキー名（例: "IS_ACTIVE"） */
    public static final String MAP_KEY_IS_ACTIVE = "IS_ACTIVE";

    /** 有効と見なす値（"1"）を定義する定数 */
    public static final String MAP_KEY_IS_ACTIVE_ON = "1";

    /** 実行対象クラス名を表すキー名（例: "CLASS_NAME"） */
    public static final String MAP_KEY_CLASS_NAME = "CLASS_NAME";

    /** 実行対象メソッド名を表すキー名（例: "METHOD_NAME"） */
    public static final String MAP_KEY_METHOD_NAME = "METHOD_NAME";

    /** ユーザー情報マップを表すキー名（例: "USER_MAP"） */
    public static final String MAP_KEY_USER_MAP = "USER_MAP";

    /** 対象データ（登録/更新/削除の対象となる1件のデータ）を表すキー名（例: "TARGET_DATA"） */
    public static final String MAP_KEY_TARGET_DATA = "TARGET_DATA";

    /** 検索条件データマップを表すキー名（例: "CONDITION_DATA"） */
    public static final String MAP_KEY_CONDITION_DATA = "CONDITION_DATA";

    /** BLOCK構成情報を表すキー名（例: "BLOCK_META"） */
    public static final String MAP_KEY_BLOCK_META = "BLOCK_META";

    /** ボタン構成情報を表すキー名（例: "BUTTOM"） */
    public static final String MAP_KEY_BUTTOM = "BUTTOM";

    /** 押下されたボタンのIDを表すキー名（例: "BUTTOM_ID"） */
    public static final String MAP_KEY_BUTTOM_ID = "BUTTOM_ID";

    /** Talon内イベント識別子を表すキー名（例: "onButton", "onSearchBefore"） */
    public static final String MAP_KEY_TLN_EVENT_ID = "TLN_EVENT_ID";

    /** TalonがInsert処理かどうかを示すキー名（boolean値） */
    public static final String MAP_KEY_TLN_IS_INSERT = "TLN_IS_INSERT";

    /** TalonがUpdate処理かどうかを示すキー名（boolean値） */
    public static final String MAP_KEY_TLN_IS_UPDATE = "TLN_IS_UPDATE";

    /** TalonがDelete処理かどうかを示すキー名（boolean値） */
    public static final String MAP_KEY_TLN_IS_DELETE = "TLN_IS_DELETE";

    /** Talonのセッション情報を表すキー名（List&lt;Map&gt;形式を想定） */
    public static final String MAP_KEY_TLN_SESSION = "TLN_SESSION";

    /** Talonのセッション情報を表すキー名（List&lt;Map&gt;形式を想定） */
    public static final String MAP_KEY_MESSAGE = "MESSAGE";

    /** Talonのセッション情報を表すキー名（List&lt;Map&gt;形式を想定） */
    public static final String MAP_KEY_STATUS = "STATUS";
}
