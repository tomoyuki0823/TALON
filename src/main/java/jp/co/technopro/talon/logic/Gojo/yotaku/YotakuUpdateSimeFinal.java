package jp.co.technopro.talon.logic.Gojo.yotaku;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_SIME_STATUS_3;
import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_YOTAKU;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.updateTkc001;

/**
 * 与託請求の締め状態を「最終締め（ステータス=3）」へ更新するロジック。
 * <p>
 * 前提：
 * <ul>
 *   <li>Nashorn 側から渡される {@code TalonParamDto} に companyCode が設定済みであること</li>
 *   <li>DBコネクション（{@code conn}）は TALON 側から供給され、本クラスではクローズしないこと</li>
 *   <li>ロールバック／コミットは呼び出し元で管理すること（本ロジックでは例外スローのみ）</li>
 * </ul>
 * 入力：
 * <ul>
 *   <li>条件データの {@code SHORI_TUKI}（文字列）</li>
 * </ul>
 * 処理：
 * <ol>
 *   <li>条件から {@code SHORI_TUKI} を取得</li>
 *   <li>{@link jp.co.technopro.talon.util.Gojo.GojoDbUtil#updateTkc001} を呼び出し、
 *       与託（{@code TK_DVS_YOTAKU}}）の締めステータスを {@code TK_DVS_SIME_STATUS_3}（最終締め）へ更新</li>
 * </ol>
 * 出力：
 * <ul>
 *   <li>正常時は {@link EventResultDto#ok()}</li>
 *   <li>必須項目不足時は {@link EventResultDto#error(String)} を返す</li>
 * </ul>
 */
public class YotakuUpdateSimeFinal extends GojoAbstractLogicBase {

    /**
     * エントリポイント。
     */
    @Override
    protected EventResultDto executeLogic() {
        return yotakuUpdateSimeFinal();
    }

    /**
     * 与託請求の締め状態を最終締めへ更新する。
     *
     * @return {@link EventResultDto}
     */
    private EventResultDto yotakuUpdateSimeFinal() {
        // 必須：処理月
        final String shoriTuki = SafeMapAccessUtil.getString(paramDto.getConditionData(), MAP_KEY_SHORI_TUKI);
        if (shoriTuki == null || shoriTuki.isBlank()) {
            return EventResultDto.error("処理月（SHORI_TUKI）が未指定です。");
        }

        // 与託 × 最終締め（3）へ更新
        // 例外は呼び出し元のトランザクション方針に従わせる（ここでは握りつぶさない）
        updateTkc001(conn, shoriTuki, TK_DVS_YOTAKU, TK_DVS_SIME_STATUS_3, paramDto.getCompanyCode());

        return EventResultDto.ok();
    }
}
