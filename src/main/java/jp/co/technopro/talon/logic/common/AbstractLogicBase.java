package jp.co.technopro.talon.logic.common;

import jp.co.technopro.logger.TalonLogger;
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
 * {@link ExecutableLogic} を実装し、共通処理やユーティリティアクセスを提供します。
 */
public abstract class AbstractLogicBase implements ExecutableLogic {

    protected Connection conn;
    protected TalonParamDto paramDto;

    /**
     * 業務ロジックの実行エントリポイントです。
     * <p>
     * TALONからこのメソッドが呼ばれ、共通パラメータをセットした後に
     * {@link #executeLogic()} を呼び出します。
     *
     * @param conn     DBコネクション（TALON管理下のためクローズ不要）
     * @param paramDto パラメータDTO（BLOCK・検索条件・対象データ等を保持）
     * @return 実行結果（正常／エラー／メッセージ等）
     */
    @Override
    public final EventResultDto run(Connection conn, TalonParamDto paramDto) {
        this.conn = conn;
        this.paramDto = paramDto;
        return executeLogic();
    }

    /**
     * 業務ロジックの本体処理を記述します。
     * <p>
     * サブクラスにて実装される必要があります。
     *
     * @return イベント処理結果
     */
    protected abstract EventResultDto executeLogic();

    protected void logInfo(String msg) {
        TalonLogger.logInfo(paramDto, msg);
    }

    protected void logInfoClassStart(String msg) {
        TalonLogger.logInfo(paramDto, "クラスイベントスタート :" + msg);
    }

    /**
     * 条件マップを取得（null-safe）。
     */
    protected Map<String, Object> getConditionData() {
        return SafeMapAccessUtil.getMap(paramDto.getConditionData());
    }

    /**
     * 対象データを取得（null-safe）。
     */
    protected Map<String, Object> getTargetData() {
        return SafeMapAccessUtil.getMap(paramDto.getTargetData());
    }

    /**
     * ユーザー情報マップを取得（null-safe）。
     */
    protected Map<String, Object> getUserMap() {
        return SafeMapAccessUtil.getMap(paramDto.getUserMap());
    }

    /**
     * 検索条件データから文字列を取得（null-safe, 空文字デフォルト）。
     */
    protected String getStringFromCondition(String key) {
        return SafeMapAccessUtil.getString(getConditionData(), key);
    }

    /**
     * 検索条件データに指定キーが存在し、値がnullや空でないかを判定します。
     */
    protected boolean hasConditionKey(String key) {
        return MapCheckUtil.hasValue(getConditionData(), key);
    }

    /**
     * ユーザーIDを取得。
     */
    protected String getUserId() {
        return (String) SafeMapAccessUtil.getMap(paramDto.getUserMap()).get(MAP_KEY_USER_ID);
    }

    /**
     * 機能IDを取得。
     */
    protected String getFuncId() {
        return (String) SafeMapAccessUtil.getMap(paramDto.getUserMap()).get(MAP_KEY_FUNC_ID);
    }

    /**
     * ブロック1の情報を取得
     */
    protected BlockDataDto getBlock1() {
        return paramDto.getBlock1();
    }

    /**
     * ブロック2の情報を取得
     */
    protected BlockDataDto getBlock2() {
        return paramDto.getBlock2();
    }

    /**
     * ブロック3の情報を取得
     */
    protected BlockDataDto getBlock3() {
        return paramDto.getBlock3();
    }

    /**
     * ブロック4の情報を取得
     */
    protected BlockDataDto getBlock4() {
        return paramDto.getBlock4();
    }

    /**
     * ブロック5の情報を取得
     */
    protected BlockDataDto getBlock5() {
        return paramDto.getBlock5();
    }

    /**
     * ブロック6の情報を取得
     */
    protected BlockDataDto getBlock6() {
        return paramDto.getBlock6();
    }

    /**
     * ブロック7の情報を取得
     */
    protected BlockDataDto getBlock7() {
        return paramDto.getBlock7();
    }

    /**
     * ブロック8の情報を取得
     */
    protected BlockDataDto getBlock8() {
        return paramDto.getBlock8();
    }

    /**
     * ブロック8の情報を取得
     */
    protected BlockDataDto getBlock9() {
        return paramDto.getBlock9();
    }
}