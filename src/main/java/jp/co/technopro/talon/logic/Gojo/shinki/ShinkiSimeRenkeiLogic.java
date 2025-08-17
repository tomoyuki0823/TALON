package jp.co.technopro.talon.logic.Gojo.shinki;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_SHINKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_SHORI_TUKI;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.loadShinkiData;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.processShinkiRecord;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.updateTkc001;
import static jp.co.technopro.talon.util.Gojo.GojoLogicUtil.getShoriTukiFromConditionData;

/**
 * 新規締め連携ロジック。
 * <p>
 * 指定の処理月について TK_SHINKI を読み込み、TK_MEMBER へ連携後に
 * 締め管理テーブル（TKC001 等）を更新する。
 * <br>
 * 0件時は正常終了（ok）とする。
 * <br>
 * トランザクション（commit/rollback）は呼び出し元で管理し、本クラスでは行わない。
 */
public class ShinkiSimeRenkeiLogic extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {
        // 入口のクラス開始ログは AbstractLogicBase#run 側で出力済み
        methodStart("ShinkiSimeRenkei 開始");
        try {
            return doShinkiSimeRenkei();
        } catch (SQLException e) {
            // ここではロールバックしない（呼び出し元で制御）
            error("新規締め連携でSQLException発生: sqlState=" + e.getSQLState()
                    + ", errorCode=" + e.getErrorCode() + ", message=" + e.getMessage(), e);
            return EventResultDto.error("処理中にDBエラーが発生しました。詳細: " + e.getMessage());
        }
    }

    /**
     * TK_SHINKI の処理対象を TK_MEMBER に反映し、締め状態を更新する。
     *
     * @return 処理結果（success=true/false、メッセージ付き）
     * @throws SQLException DBアクセス時のエラー（呼び出し元でトランザクション制御）
     */
    private EventResultDto doShinkiSimeRenkei() throws SQLException {
        final String companyCd = paramDto.getCompanyCode();
        if (companyCd == null || companyCd.isBlank()) {
            return EventResultDto.error("会社コードが未指定です。");
        }

        final String shoriTuki = getShoriTukiFromConditionData(paramDto);
        if (shoriTuki == null || shoriTuki.isBlank()) {
            return EventResultDto.error(MSG_NON_SHORI_TUKI);
        }

        info("新規締め連携 開始: company=" + companyCd + ", 処理月=" + shoriTuki);

        final List<Map<String, Object>> shinkiList = loadShinkiData(conn, shoriTuki);
        if (shinkiList == null || shinkiList.isEmpty()) {
            info("新規連携対象 0 件: 処理月=" + shoriTuki + ", company=" + companyCd);
            return EventResultDto.ok("新規連携対象はありません（0件）。");
        }

        int success = 0;
        for (Map<String, Object> record : shinkiList) {
            // processShinkiRecord 内で insert/update を実施する前提
            processShinkiRecord(conn, companyCd, record);
            success++;
        }

        updateTkc001(conn, shoriTuki, TK_DVS_SHINKI, companyCd);

        info("新規締め連携 完了: 対象=" + shinkiList.size() + " 件, 成功=" + success
                + ", 処理月=" + shoriTuki + ", company=" + companyCd);

        return EventResultDto.ok("新規締め連携が正常終了しました（成功 " + success + " 件）。");
    }
}
