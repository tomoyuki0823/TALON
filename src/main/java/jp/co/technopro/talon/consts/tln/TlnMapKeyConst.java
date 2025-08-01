package jp.co.technopro.talon.consts.tln;

/**
 * イベント処理やロジック実行時に使用される Map の共通キー定数を定義するユーティリティクラス。
 * <p>
 * {@link java.util.Map Map&lt;String, Object&gt;} を介して各種情報をやり取りする際に、
 * 使用するキー名のタイポ防止と共通化を目的とします。
 * </p>
 */
public final class TlnMapKeyConst {

    private TlnMapKeyConst() {
        // インスタンス化禁止
    }

    // === 基本識別情報 ===

    /** 機能IDを表すキー名（例: "FUNC_ID"） */
    public static final String MAP_KEY_FUNC_ID = "FUNC_ID";

    /** ユーザIDを表すキー名（例: "USER_ID"） */
    public static final String MAP_KEY_USER_ID = "USER_ID";

    /** イベントIDを表すキー名（例: "EVENT_ID"） */
    public static final String MAP_KEY_EVENT_ID = "EVENT_ID";

    /** ロジックIDを表すキー名（例: "LOGIC_ID"） */
    public static final String MAP_KEY_LOGIC_ID = "LOGIC_ID";

    /** 実行対象クラス名を表すキー名（例: "CLASS_NAME"） */
    public static final String MAP_KEY_CLASS_NAME = "CLASS_NAME";

    /** 実行対象メソッド名を表すキー名（例: "METHOD_NAME"） */
    public static final String MAP_KEY_METHOD_NAME = "METHOD_NAME";


    // === 状態フラグ ===

    /** 有効フラグを表すキー名（例: "IS_ACTIVE"） */
    public static final String MAP_KEY_IS_ACTIVE = "IS_ACTIVE";

    /** 有効と見なす値（"1"）を定義する定数 */
    public static final String MAP_KEY_IS_ACTIVE_ON = "1";

    /** 成功フラグを表すキー名（boolean想定） */
    public static final String MAP_KEY_SUCCESS = "success";


    // === データ関連 ===

    /** ユーザー情報マップを表すキー名（例: "USER_MAP"） */
    public static final String MAP_KEY_USER_MAP = "USER_MAP";

    /** 対象データ（登録/更新/削除の対象となる1件のデータ）を表すキー名 */
    public static final String MAP_KEY_TARGET_DATA = "TARGET_DATA";

    /** 検索条件データマップを表すキー名 */
    public static final String MAP_KEY_CONDITION_DATA = "CONDITION_DATA";


    // === Talonイベント関連 ===

    /** Talon内イベント識別子を表すキー名（例: "onButton"） */
    public static final String MAP_KEY_TLN_EVENT_ID = "TLN_EVENT_ID";

    /** TalonがInsert処理かどうかを示すキー名（boolean値） */
    public static final String MAP_KEY_TLN_IS_INSERT = "TLN_IS_INSERT";

    /** TalonがUpdate処理かどうかを示すキー名（boolean値） */
    public static final String MAP_KEY_TLN_IS_UPDATE = "TLN_IS_UPDATE";

    /** TalonがDelete処理かどうかを示すキー名（boolean値） */
    public static final String MAP_KEY_TLN_IS_DELETE = "TLN_IS_DELETE";

    /** TalonがDelete処理かどうかを示すキー名（boolean値） */
    public static final String MAP_KEY_LOGGER = "LOGGER";


    // === セッション・メッセージ・状態 ===

    /** Talonのセッション情報を表すキー名（List&lt;Map&gt;形式を想定） */
    public static final String MAP_KEY_TLN_SESSION = "TLN_SESSION";

    /** メッセージ（TALONエラーやINFOなど）を表すキー名 */
    public static final String MAP_KEY_MESSAGE = "MESSAGE";

    /** ステータス（処理状態や判定など）を表すキー名 */
    public static final String MAP_KEY_STATUS = "STATUS";


    // === ブロック・ボタン構成関連 ===

    /** BLOCK構成情報を表すキー名（例: "BLOCK_META"） */
    public static final String MAP_KEY_BLOCK_META = "BLOCK_META";

    /** ボタン構成情報を表すキー名（例: "BUTTOM"） */
    public static final String MAP_KEY_BUTTOM = "BUTTOM";

    /** 押下されたボタンのIDを表すキー名（例: "BUTTOM_ID"） */
    public static final String MAP_KEY_BUTTOM_ID = "BUTTOM_ID";


    // === その他（会社コードなど） ===

    /** 対象会社コードを表すキー名（例: "Gojo"） */
    public static final String MAP_KEY_COMPANY_CODE = "COMPANY_CODE";

    /** 共通会社コード（共通DB用途など）を表すキー名 */
    public static final String MAP_KEY_COMPANY_CODE_COMMON = "COMPANY_CODE_COMMON";

    /** 共通会社コード（共通DB用途など）を表すキー名 */
    public static final String MAP_KEY_SIKIBETU_CODE = "SIKIBETU_CODE";

    /** 共通会社コード（共通DB用途など）を表すキー名 */
    public static final String MAP_KEY_KEY_CODE = "KEY_CODE";

    // ==============================
    // 登録・更新情報
    // ==============================

    /** 作成日時 */
    public static final String MAP_KEY_CREATED_DATE = "CREATED_DATE";

    /** 作成者ユーザーID */
    public static final String MAP_KEY_CREATED_BY = "CREATED_BY";

    /** 作成プログラム名 */
    public static final String MAP_KEY_CREATED_PRG_NM = "CREATED_PRG_NM";

    /** 更新日時 */
    public static final String MAP_KEY_UPDATED_DATE = "UPDATED_DATE";

    /** 更新者ユーザーID */
    public static final String MAP_KEY_UPDATED_BY = "UPDATED_BY";

    /** 更新プログラム名 */
    public static final String MAP_KEY_UPDATED_PRG_NM = "UPDATED_PRG_NM";

    /** 単票ブロック */
    public static final String MAP_KEY_CARD = "CARD";

    /** 明細ブロック */
    public static final String MAP_KEY_LIST = "LIST";

    /** データ */
    public static final String MAP_KEY_DATA = "DATA";

    /** MAP_KEY_TYPE */
    public static final String MAP_KEY_TYPE = "TYPE";
}
