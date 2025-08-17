package jp.co.technopro.talon.logic.Gojo.sime;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;

/**
 * 締め連携ファサード。
 * <p>
 * Nashorn からは本クラスのみを呼び出し、内部で {@link GojoSimeRenkeiDispatcher}
 * により TK_DVS ごとのロジックを呼び分ける。
 * <br>
 * トランザクション（commit/rollback）は呼び出し元で管理。
 */
public class GojoSimeRenkeiFacadeLogic extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {
        // ここでログ整備（開始 + 主要パラメータ）
        logInfoClassStart(getClass().getSimpleName());

        final String companyCd = paramDto.getCompanyCode();
        if (companyCd == null || companyCd.isBlank()) {
            return EventResultDto.error("会社コードが未指定です。");
        }

        // conditionData から必要要素は Dispatcher 側で検証するが、
        // ここでも主要要素をログに残すと運用追跡が楽。
        logInfo("締め連携 実行開始: company=" + companyCd);

        // 実体は Dispatcher に委譲（TK_DVS のみで分岐）
        return GojoSimeRenkeiDispatcher.dispatch(conn, paramDto);
    }
}
