package jp.co.technopro.talon.logic.Gojo.choju;

import jp.co.technopro.talon.consts.Gojo.*;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.sql.Gojo.GojoSqlLoader;
import jp.co.technopro.talon.sql.common.SqlLoader;
import jp.co.technopro.talon.util.common.DbUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChojuInitLogic extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {

        String SHORI_TUKI = this.getShoriTuki();

        //空チェック
        if (SHORI_TUKI == null) {
            return EventResultDto.error(GojoMessagesConst.MSG_ERROR_EMPTY);
        }

        //数値チェック
        if (!SHORI_TUKI.matches("^\\d{6}$")) {
            return EventResultDto.error(SHORI_TUKI + GojoMessagesConst.MSG_ERROR_INVALID);
        }

        //データチェック
        Map<String, Object> map = new HashMap<>();
        map.put(GojoMapKeyConst.MAP_KEY_SHORI_TUKI, SHORI_TUKI);
        int count = DbUtil.getCount(conn, GojoTableNameConst.TABLE_TK_CHOJU_01, map, paramDto.getCompanyCode());
        if (count != 0) {
            return EventResultDto.ok(GojoMessagesConst.MSG_OK_ALREADY);
        }

        //クエリを取得
        //データを取得してそのまま保存する
        try {
            DbUtil.setSqlLoader(new GojoSqlLoader());
            List<Map<String, Object>> result = DbUtil.selectById(conn, GojoSqlKeyConst.SQL_KEY_SEARCH_IWAI_KIN, paramDto.getCompanyCode(),
                    SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI).getMapListResult();
            for (Map<String, Object> map2 : result) {
                DbUtil.insertByMapEx(conn, paramDto.getCompanyCode(), GojoTableNameConst.TABLE_TK_CHOJU_01, map2, false);
            }
        } catch (SQLException e) {
            return EventResultDto.error(GojoMessagesConst.MSG_ERROR_FAILED);// + iwai);
        }

        return EventResultDto.ok();

    }

}
