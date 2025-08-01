package jp.co.technopro.talon.logic.Gojo.common;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_NO;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_DUPLICATE_GENSYOKU;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_SHINKI;
import static jp.co.technopro.talon.util.common.DbUtil.isTableEmpty;

public class ChkDuplicateLogic extends AbstractLogicBase {
    @Override
    protected EventResultDto executeLogic() {
        return null;
    }

     /**
     * TK_SHINKI テーブルに指定された会員番号（no）が既に存在するかをチェックします。
     *
     * @param conn     DBコネクション
     * @param paramDto パラメータマップ（"no" キーを含む必要があります）
     * @return 重複があれば success=false とエラーメッセージ、なければ success=true と正常メッセージを含む結果Map
     */
     private EventResultDto chkDuplicate(Connection conn, TalonParamDto paramDto)  {

        Map<String, Object> params = paramDto.getTargetData();
        Object noObj = params.get(MAP_KEY_NO);
        if (noObj == null) {

            return EventResultDto.error("会員番号（NO）が指定されていません。");
        }

        int no;
        try {
            no = Integer.parseInt(noObj.toString());
        } catch (NumberFormatException e) {
            return EventResultDto.error("会員番号（NO）が数値でありません。");
        }
        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_NO, no);

        boolean duplicate = !isTableEmpty(conn, TABLE_TK_SHINKI, whereMap, paramDto.getCompanyCode());

        if (duplicate) {
            return EventResultDto.error(MSG_DUPLICATE_GENSYOKU);

        } else {
            return EventResultDto.ok();
        }
    }
}
