package jp.co.technopro.talon.logic.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.util.common.DbUtil;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;
import jp.co.technopro.talon.util.common.StringUtil;

import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_T_IRYO_SOKIN;

public class IryoChkMemberDuplicate extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {
        return chkMemberDuplicate();
    }

    /**
     * 医療費登録画面(機能ID：TK_IRYO_02)にて
     * 以下の重複チェックを行う。
     * ①会員番号(TK_NO) ②療養年月(RYOYO_NENGETU)
     * ・重複がなければ OK（登録可とする）
     * ・重複があれば、エラーメッセージで返す
     * ・入力が不正（未設定）はチェックせず OK（登録可とする）
     *
     * @return EventResultDto
     */

    private EventResultDto chkMemberDuplicate() {

        Map<String, Object> map = getTargetData();

        String tkNo = SafeMapAccessUtil.getString(map, MAP_KEY_TK_NO);
        String ryoyoNengetu = SafeMapAccessUtil.getString(map, MAP_KEY_RYOYO_NENGETU);

        // 重複チェック（TK_T_IRYO_SOKIN を対象）
        if (StringUtil.isNullOrEmpty(tkNo) || StringUtil.isNullOrEmpty(ryoyoNengetu)) {

            // 入力不正（未設定）はスルー
            return EventResultDto.ok();
        }

        // 重複チェック
         int count = DbUtil.getCount(
             conn,
             TABLE_TK_T_IRYO_SOKIN, // 定数を利用
             Map.of(MAP_KEY_TK_NO, tkNo, MAP_KEY_RYOYO_NENGETU, ryoyoNengetu),
             paramDto.getCompanyCode()
         );

        if (count > 0)
             return EventResultDto.error("特別会員番号 : " + tkNo + " は同一療養年月で登録済みです。");

        // 重複なしなら登録
        return EventResultDto.ok();
    }
}