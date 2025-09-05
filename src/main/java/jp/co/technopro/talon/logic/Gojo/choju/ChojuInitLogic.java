package jp.co.technopro.talon.logic.Gojo.choju;

import jp.co.technopro.talon.consts.Gojo.GojoMessagesConst;
import jp.co.technopro.talon.consts.Gojo.GojoSqlXmlPathConst;
import jp.co.technopro.talon.consts.Gojo.GojoTableNameConst;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.sql.common.SqlLoader;
import jp.co.technopro.talon.util.common.DbUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChojuInitLogic extends GojoAbstractLogicBase {


    @Override
    protected EventResultDto executeLogic() {

        // TODO GojoAbstractLogicBaseにgetShoriTukiを用意してありますので、そちらを利用してください。
        String SHORI_TUKI = (String)paramDto.getConditionData().get("SHORI_TUKI");

        //空チェック
        if(SHORI_TUKI == null){
            return EventResultDto.error(GojoMessagesConst.MSG_ERROR_EMPTY);
        }

        //数値チェック
        if(!SHORI_TUKI.matches("^\\d{6}$")){
            return EventResultDto.error(SHORI_TUKI + GojoMessagesConst.MSG_ERROR_INVALID);
        }

        //データチェック
        // TODO一般的に 左側の定義は抽象化するのが基本です。 HashMap→Mapとしてください。これはListも同様です。
        HashMap<String, Object> map = new HashMap<>();
        // 仮になかった場合、定数クラスに定義をお願いいたします。
        // TODO マップキーは定数クラスから取得してください。
        map.put("SHORI_TUKI", SHORI_TUKI);
        int count = DbUtil.getCount(conn, GojoTableNameConst.TABLE_TK_CHOJU_01, map, paramDto.getCompanyCode());
        if(count != 0){
            return EventResultDto.ok(GojoMessagesConst.MSG_OK_ALREADY);
        }

        //クエリを取得
        SqlLoader loader = new SqlLoader(GojoSqlXmlPathConst.SQL_GOJO_CHOJU_IWAI);
        DbUtil.setSqlLoader(loader);
        // TODO 定数化は？？
        String iwai = DbUtil.getSql("SEARCH_IWAI_KIN");

        //データを取得してそのまま保存する
        try{
            List<Map<String, Object>> result = DbUtil.select(conn, paramDto.getCompanyCode(), iwai, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI, SHORI_TUKI);
            for(Map<String, Object> map2 : result){
                DbUtil.insertByMapEx(conn, paramDto.getCompanyCode(), GojoTableNameConst.TABLE_TK_CHOJU_01, map2, true);
            }
        }catch(SQLException e){
            return EventResultDto.error(GojoMessagesConst.MSG_ERROR_FAILED + iwai);
        }

        return EventResultDto.ok();

    }

}
