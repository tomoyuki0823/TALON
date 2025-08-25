package jp.co.technopro.talon.logic.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.util.common.DbUtil;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;

import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_IRYO;

public class IryoChkDuplicateRyoyo extends GojoAbstractLogicBase {


    @Override
    protected EventResultDto executeLogic() {
        return chkDuplicateRyoyoDt();
    }

    private EventResultDto chkDuplicateRyoyoDt() {

        if (paramDto.isTlnIsDelete()) return EventResultDto.ok();
        if (paramDto.isTlnIsUpdate()) return EventResultDto.ok();
        Map<String, Object> map = getTargetData();
        String zoku = SafeMapAccessUtil.getString(map, MAP_KEY_ZOKU);
        String ryoyoNengetu = SafeMapAccessUtil.getString(map, MAP_KEY_RYOYO_NENGETU);
        String tkno = SafeMapAccessUtil.getString(map, MAP_KEY_TK_NO);

        int count = DbUtil.getCount(conn, TABLE_TK_IRYO, Map.of(MAP_KEY_ZOKU, zoku, MAP_KEY_RYOYO_NENGETU, ryoyoNengetu, MAP_KEY_TK_NO, tkno), paramDto.getCompanyCode());

        if (count > 0) return EventResultDto.error("特別会員番号 : " + tkno + " の情報は同一療養年月で登録済みです。");
        return EventResultDto.ok();
    }
}
