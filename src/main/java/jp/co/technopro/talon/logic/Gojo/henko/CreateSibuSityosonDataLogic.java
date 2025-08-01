package jp.co.technopro.talon.logic.Gojo.henko;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_SHORI_TUKI;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.delInsTkHenko03;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.getSibuSityosonData;

public class CreateSibuSityosonDataLogic extends AbstractLogicBase {
    @Override
    protected EventResultDto executeLogic() {
        try {
            return createSibuSityosonData(conn, paramDto);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * VIEW_TK_HENKO_03 から支部・市町村単位の集計データを取得し、TK_HENKO_03 テーブルに月次データとして登録します。
     * <p>
     * 以下の処理を実行します：
     * <ol>
     *   <li>処理月（SHORI_TUKI）が指定されているかをチェック（未指定ならエラー返却）</li>
     *   <li>VIEW_TK_HENKO_03 より支部・市町村の一覧データを取得</li>
     *   <li>各レコードについて TK_HENKO_03 テーブルの同月データを削除した上で、レコードを INSERT</li>
     * </ol>
     * 本メソッドは {@code SQLException} を投げるが、ロールバックは呼び出し元が責務を持つ。
     * </p>
     *
     * @param conn      DBコネクション（外部で管理されたものを使用。クローズはしません）
     * @param paramDto  TalonParamDto（処理月、会社コードなどを含む）
     * @return 正常終了時は {@link EventResultDto#ok()}、処理月未指定時は {@link EventResultDto#error(String)} を返す
     * @throws SQLException SQL 実行中にエラーが発生した場合（ロールバックは呼び出し元で実施）
     */
    private EventResultDto createSibuSityosonData(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> params = paramDto.getConditionData();
        String shoriTuki = (String) params.get(MAP_KEY_SHORI_TUKI);

        if (shoriTuki == null || shoriTuki.isBlank()) {
            return EventResultDto.error(MSG_NON_SHORI_TUKI);
        }

        List<Map<String, Object>> sibuMapList = null;
        try {
            sibuMapList = getSibuSityosonData(conn, paramDto);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            for (Map<String, Object> record : sibuMapList) {
                delInsTkHenko03(conn, shoriTuki, record, paramDto);
            }

            return EventResultDto.ok();

        } catch (SQLException ex) {
            // ロールバックは呼び出し元で制御される前提
            throw ex;
        }
    }

}
