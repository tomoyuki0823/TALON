package jp.co.technopro.talon.logic.common;

import jp.co.technopro.logger.TpiLogger;
import jp.co.technopro.talon.dto.common.BlockDataDto;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.util.common.MapCheckUtil;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;

import java.sql.Connection;
import java.util.Map;

import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.MAP_KEY_FUNC_ID;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.MAP_KEY_USER_ID;

/**
 * TALONから呼び出される全ロジックの共通基底クラス。
 * <p>
 * {@link ExecutableLogic} を実装し、共通の実行エントリ・ロギング・
 * ParamDTOの安全アクセスユーティリティを提供します。
 * <br>
 * トランザクション（commit/rollback）は呼び出し元で管理します。本クラスでは行いません。
 */
public abstract class AbstractLogicBase implements ExecutableLogic {

    /** 統一フォーマットで出力するアプリ共通ロガー */
    private static final TpiLogger log = TpiLogger.getLogger(AbstractLogicBase.class);

    /** 呼び出し元から受領する DB コネクション（TALON管理下のため本クラスではクローズしない） */
    protected Connection conn;

    /** TALON → Java のパラメータDTO（BLOCK/USER/CONDITION/TARGET 等を保持） */
    protected TalonParamDto paramDto;

    /**
     * 業務ロジックの実行エントリポイント。
     * <p>
     * TALONプラットフォームから本メソッドが呼ばれ、共通パラメータをセットした後に
     * {@link #executeLogic()} を呼び出します。
     *
     * @param conn     DBコネクション（autoCommit=false 推奨、クローズ不要）
     * @param paramDto パラメータDTO（BLOCK・検索条件・対象データ等を保持）
     * @return 実行結果（正常／エラー／メッセージ等）
     * @throws RuntimeException 実行中の予期せぬ例外は呼び出し元でロールバックされる前提
     */
    @Override
    public final EventResultDto run(Connection conn, TalonParamDto paramDto) {
        this.conn = conn;
        this.paramDto = paramDto;

        final String logicName = this.getClass().getSimpleName();
        final String eventId = (paramDto != null) ? paramDto.getEventId() : "null";

        log.classStart(logicName);
        log.info("イベント開始 [eventId=" + eventId + "]");

        try {
            return executeLogic();
        } catch (Exception ex) {
            log.error("ロジック実行中にエラーが発生: " + logicName, ex);
            // 方針：ここではロールバックしない（呼び出し元で制御）
            throw ex;
        }
    }

    /**
     * 業務ロジックの本体処理を実装してください。
     *
     * @return イベント処理結果
     */
    protected abstract EventResultDto executeLogic();

    // ---------------------------------------------------------------------
    // 共通ロギング ヘルパ
    // ---------------------------------------------------------------------

    /** INFOログ出力（統一フォーマット） */
    protected void info(String msg) { log.info(msg); }

    /** WARNログ出力（統一フォーマット） */
    protected void warn(String msg) { log.warn(msg); }

    /** ERRORログ出力（メッセージのみ、統一フォーマット） */
    protected void error(String msg) { log.error(msg); }

    /** ERRORログ出力（例外付き、統一フォーマット） */
    protected void error(String msg, Throwable ex) { log.error(msg, ex); }

    /** クラス開始ログ（任意で再度出したい場面向け） */
    protected void classStart(String className) { log.classStart(className); }

    /** メソッド開始ログ（呼び出し元メソッド名を自動出力） */
    protected void methodStart() { log.methodStart(); }

    /** メソッド開始ログ（補足付き、呼び出し元メソッド名を自動出力） */
    protected void methodStart(String additionalMsg) { log.methodStart(additionalMsg); }

    // ---------------------------------------------------------------------
    // 既存互換（非推奨）：段階的移行のため残置。順次置換推奨。
    // ---------------------------------------------------------------------

    /** @deprecated {@link #info(String)} へ置換してください。 */
    @Deprecated protected void logInfo(String msg) { info(msg); }

    /** @deprecated {@link #classStart(String)} へ置換してください。 */
    @Deprecated protected void logInfoClassStart(String msg) { classStart(msg); }

    /** @deprecated {@link #methodStart()} へ置換してください。 */
    @Deprecated protected void logInfoMethodStart() { methodStart(); }

    /** @deprecated {@link #methodStart(String)} へ置換してください。 */
    @Deprecated protected void logInfoMethodStart(String msg) { methodStart(msg); }

    /** @deprecated {@link #error(String)} へ置換してください。 */
    @Deprecated protected void logError(String msg) { error(msg); }

    /** @deprecated {@link #error(String, Throwable)} へ置換してください。 */
    @Deprecated protected void logError(String msg, Throwable ex) { error(msg, ex); }

    // ---------------------------------------------------------------------
    // ParamDTO 安全アクセサ（null-safe）
    // ---------------------------------------------------------------------

    /** conditionData を取得（null-safe） */
    protected Map<String, Object> getConditionData() {
        return (paramDto == null) ? Map.of() : SafeMapAccessUtil.getMap(paramDto.getConditionData());
    }

    /** targetData を取得（null-safe） */
    protected Map<String, Object> getTargetData() {
        return (paramDto == null) ? Map.of() : SafeMapAccessUtil.getMap(paramDto.getTargetData());
    }

    /** userMap を取得（null-safe） */
    protected Map<String, Object> getUserMap() {
        return (paramDto == null) ? Map.of() : SafeMapAccessUtil.getMap(paramDto.getUserMap());
    }

    /** condition から文字列を取得（null/空は空文字を返さずに null を返す方針の場合は適宜変更） */
    protected String getStringFromCondition(String key) {
        return SafeMapAccessUtil.getString(getConditionData(), key);
    }

    /** condition にキーが存在し値があるかを判定 */
    protected boolean hasConditionKey(String key) {
        return MapCheckUtil.hasValue(getConditionData(), key);
    }

    /** ユーザーIDを取得 */
    protected String getUserId() {
        return (String) getUserMap().get(MAP_KEY_USER_ID);
    }

    /** 機能IDを取得 */
    protected String getFuncId() {
        return (String) getUserMap().get(MAP_KEY_FUNC_ID);
    }

    // ---------------------------------------------------------------------
    // BLOCK アクセサ
    // ---------------------------------------------------------------------

    protected BlockDataDto getBlock1() { return (paramDto != null) ? paramDto.getBlock1() : null; }
    protected BlockDataDto getBlock2() { return (paramDto != null) ? paramDto.getBlock2() : null; }
    protected BlockDataDto getBlock3() { return (paramDto != null) ? paramDto.getBlock3() : null; }
    protected BlockDataDto getBlock4() { return (paramDto != null) ? paramDto.getBlock4() : null; }
    protected BlockDataDto getBlock5() { return (paramDto != null) ? paramDto.getBlock5() : null; }
    protected BlockDataDto getBlock6() { return (paramDto != null) ? paramDto.getBlock6() : null; }
    protected BlockDataDto getBlock7() { return (paramDto != null) ? paramDto.getBlock7() : null; }
    protected BlockDataDto getBlock8() { return (paramDto != null) ? paramDto.getBlock8() : null; }
    protected BlockDataDto getBlock9() { return (paramDto != null) ? paramDto.getBlock9() : null; }
}
