package jp.co.technopro.talon.logic.Gojo.common;

import jp.co.technopro.talon.dto.common.BlockDataDto;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;

import java.sql.Connection;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_TK_NO;
import static jp.co.technopro.talon.util.common.MapCheckUtil.isEmptyAnyBlank;

/**
 * GOJOプロジェクト専用の抽象基底ロジッククラス。
 * 基本的なパラメータ取得・共通処理を提供します。
 */
public abstract class GojoAbstractLogicBase extends AbstractLogicBase {

    /**
     * 継承クラスで必ず実装すべきロジック実行メソッド。
     *
     * @return EventResultDto 処理結果DTO
     */
    @Override
    protected abstract EventResultDto executeLogic();

    /**
     * 条件マップまたはブロックから特別会員番号(TK_NO)を取得します。
     * 各種データを優先順位に基づいて探索し、最初に発見されたTK_NOを返却します。
     *
     * @return 特別会員番号 (存在しない場合は空文字を返却)
     */
    protected String getTkNo() {
        String tkNo;

        if (!isEmptyAnyBlank(getConditionData(), MAP_KEY_TK_NO)) {
            return SafeMapAccessUtil.getString(getConditionData(), MAP_KEY_TK_NO);
        }
        if (!isEmptyAnyBlank(getTargetData(), MAP_KEY_TK_NO)) {
            return SafeMapAccessUtil.getString(getTargetData(), MAP_KEY_TK_NO);
        }

        for (int i = 1; i <= 9; i++) {
            tkNo = getValueFromBlock(i, MAP_KEY_TK_NO);
            if (!tkNo.isBlank()) {
                return tkNo;
            }
        }

        return "";
    }

    /**
     * 条件マップまたはブロックから特別会員番号(TK_NO)を取得します。
     * 各種データを優先順位に基づいて探索し、最初に発見されたTK_NOを返却します。
     *
     * @return 特別会員番号 (存在しない場合は空文字を返却)
     */
    protected String getShoriTuki() {
        String shoriTuki;

        if (!isEmptyAnyBlank(getConditionData(), MAP_KEY_SHORI_TUKI)) {
            return SafeMapAccessUtil.getString(getConditionData(), MAP_KEY_SHORI_TUKI);
        }
        if (!isEmptyAnyBlank(getTargetData(), MAP_KEY_SHORI_TUKI)) {
            return SafeMapAccessUtil.getString(getTargetData(), MAP_KEY_SHORI_TUKI);
        }

        for (int i = 1; i <= 9; i++) {
            shoriTuki = getValueFromBlock(i, MAP_KEY_SHORI_TUKI);
            if (!shoriTuki.isBlank()) {
                return shoriTuki;
            }
        }

        return "";
    }

    /**
     * 指定されたブロック番号から特別会員番号(TK_NO)を取得します。
     *
     * @param blockNumber ブロック番号（1〜9）
     * @return TK_NO（存在しない場合は空文字を返却）
     */
    private String getValueFromBlock(int blockNumber, String key) {
        BlockDataDto block = getBlock(blockNumber);
        if (block != null && block.isCard()) {
            if (!isEmptyAnyBlank(block.getCardData(), key)) {
                return SafeMapAccessUtil.getString(block.getCardData(), key);
            }
        }
        return "";
    }

    /**
     * ブロック番号に基づいて該当するBlockDataDtoを返却します。
     *
     * @param blockNumber ブロック番号（1〜9）
     * @return BlockDataDto 該当するブロックデータ
     */
    private BlockDataDto getBlock(int blockNumber) {
        switch (blockNumber) {
            case 1:
                return getBlock1();
            case 2:
                return getBlock2();
            case 3:
                return getBlock3();
            case 4:
                return getBlock4();
            case 5:
                return getBlock5();
            case 6:
                return getBlock6();
            case 7:
                return getBlock7();
            case 8:
                return getBlock8();
            case 9:
                return getBlock9();
            default:
                return null;
        }
    }
}