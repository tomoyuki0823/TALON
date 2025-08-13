package jp.co.technopro.talon.logic.Gojo.common;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_GOJYO_TAIKAI_CD;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_NO;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_GEN_T_KAIIN;
import static jp.co.technopro.talon.util.common.DbUtil.isTableEmpty;

public class ChkGensyokuTaikaiLogic extends GojoAbstractLogicBase {
    @Override
    protected EventResultDto executeLogic() {

        logInfoClassStart(getClass().getSimpleName());
        return chkGensyoku(conn, paramDto);
    }

    /**
     * 指定された会員番号が既に退会済みであるかを確認します。
     *
     * <p>GEN_T_KAIIN テーブルにおいて、NO が一致し GOJYO_TAIKAI_CD が NULL のレコードが
     * 存在しない（つまり退会済み）場合に成功と判断します。</p>
     *
     * @param conn     DB接続
     * @param paramDto パラメータ（MAP_KEY_NO を含む必要あり）
     * @return 成功時は true、退会していない場合は false とエラーメッセージ
     * @throws SQLException DBアクセスエラー
     */
    private EventResultDto chkGensyoku(Connection conn, TalonParamDto paramDto)  {

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

        // 退会していないデータが存在するかを確認
        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_NO, no);
        whereMap.put(MAP_KEY_GOJYO_TAIKAI_CD, null);  // null指定 → IS NULL 検索

        boolean isTaikai = isTableEmpty(conn, TABLE_GEN_T_KAIIN, whereMap, paramDto.getCompanyCode());

        if (!isTaikai) {
            return EventResultDto.error("現職会員番号: " + no + " は未退会です。");
        }

        return EventResultDto.ok();
    }

}
