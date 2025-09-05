package jp.co.technopro.talon.logic.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.util.Gojo.GojoSoftFlagService;
import jp.co.technopro.talon.util.Gojo.GojoYearMonthCheckUtil;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;
import jp.co.technopro.talon.util.common.StringUtil;

import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.FLG_ON;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_TK_NO;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_ZOKU;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_IRYO;

public class IryoChkRyoyoNengetu extends GojoAbstractLogicBase {
    @Override
    protected EventResultDto executeLogic() {
        return setRyoyoNengetuSoft();
    }

    /**
     * 処理月（syoriTuki）と療養年月（ryoyoNengetu）の月差チェックを行い、
     * しきい値（38か月）以上ならDBにフラグを立てる「ソフトエラー」実装。
     * <p>
     * ・例外は投げず、常に正常系の EventResult を返す。<br>
     * ・入力が未設定/形式不正（yyyyMM 以外）はフラグ対象にしない（スルー）。
     * ・トランザクションは呼び出し側ポリシー（外側）に従う。
     *
     * @return 常に正常（OK）結果
     */
    private EventResultDto setRyoyoNengetuSoft() {

        logInfoMethodStart();

        if (paramDto.isTlnIsDelete()) return EventResultDto.ok();
        if (paramDto.isTlnIsUpdate()) return EventResultDto.ok();
        Map<String, Object> map = paramDto.getTargetData();
        String syoriTuki = SafeMapAccessUtil.getString(map, MAP_KEY_SHORI_TUKI);
        String ryoyoNengetu = SafeMapAccessUtil.getString(map, MAP_KEY_RYOYO_NENGETU);

        // TRUE=問題なし(フラグ不要) / FALSE=問題あり(フラグ要)
        boolean ok = GojoYearMonthCheckUtil.isDiffLessThanMonths(syoriTuki, ryoyoNengetu, 38);
        if (ok) {
            return EventResultDto.ok();
        }

        // WHERE必須キー（誤更新防止）
        String tk_no = SafeMapAccessUtil.getString(map, MAP_KEY_TK_NO);
        String zoku = SafeMapAccessUtil.getString(map, MAP_KEY_ZOKU);
        if (StringUtil.isNullOrEmpty(tk_no) ||
                StringUtil.isNullOrEmpty(zoku) ||
                StringUtil.isNullOrEmpty(syoriTuki) ||
                StringUtil.isNullOrEmpty(ryoyoNengetu)) {
            return EventResultDto.ok();
        }

        // WHEREは順序固定で
        Map<String, Object> where = new java.util.LinkedHashMap<>();
        where.put(MAP_KEY_TK_NO, tk_no);
        where.put(MAP_KEY_ZOKU, zoku);
        where.put(MAP_KEY_SHORI_TUKI, syoriTuki);
        where.put(MAP_KEY_RYOYO_NENGETU, ryoyoNengetu);

        // ログなし・例外外出しなし（静かに更新）
        GojoSoftFlagService.updateFlagSilently(
                conn,
                TABLE_TK_IRYO,
                "RYOYO_YM_JIKO_FLG",
                FLG_ON,
                where
        );
        return EventResultDto.ok();
    }
}
