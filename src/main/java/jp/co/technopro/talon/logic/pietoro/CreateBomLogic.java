package jp.co.technopro.talon.logic.pietoro;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;
import jp.co.technopro.talon.util.common.StringCheckUtil;
import jp.co.technopro.talon.util.common.StoredProcUtil;
import jp.co.technopro.talon.db.common.DbConnections;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * BOM作成ロジック。
 * <p>
 * dbo.SP_BUILD_TRACE_RESULT を実行し、REQUEST_ID を取得します。<br>
 * 入力:  @START_LOT varchar(20), @DIRECTION char(1), @REQUEST_ID uniqueidentifier(=NULLならNEWID()), @INCLUDE_ROOT bit<br>
 * 出力:  なし（最後に SELECT @REQUEST_ID AS REQUEST_ID を返す）
 * </p>
 *
 * <h3>設計メモ</h3>
 * <ul>
 *   <li>paramDto/conditionData が null の場合はエラー返却</li>
 *   <li>START_LOT は "START_LOT_NO" が無ければ "START_LOT" を代替キーとして参照</li>
 *   <li>DIRECTION は 'F' / 'B' のみ許可（大小・前後空白を正規化）</li>
 *   <li>REQUEST_ID は Java 側で UUID 採番して渡す（NULL 明示渡しを回避）</li>
 *   <li>外部から渡された Connection はクローズしない／自前取得のみクローズ</li>
 * </ul>
 */
public class CreateBomLogic extends AbstractLogicBase {

    private static final String COMPANY_CD = "pietoro";
    private static final String PROC_NAME  = "dbo.SP_BUILD_TRACE_RESULT";

    @Override
    protected EventResultDto executeLogic() {
        return createBom();
    }

    /**
     * ストアド実行本体（ロールバックは呼び出し元で制御）。
     */
    private EventResultDto createBom() {

        // --- null セーフ: paramDto/conditionData
        if (paramDto == null) {
            logInfo("paramDto が null です。");
            return EventResultDto.error("内部エラー: パラメータが取得できませんでした。");
        }
        Map<String, Object> cond = paramDto.getConditionData();
        if (cond == null) {
            logInfo("conditionData が null です。");
            return EventResultDto.error("条件データが未設定です。");
        }

        // --- 入力取り出し（キーの揺れに対応: START_LOT_NO 優先、無ければ START_LOT）
        String startLot = coalesce(
                SafeMapAccessUtil.getString(cond, "START_LOT_NO"),
                SafeMapAccessUtil.getString(cond, "START_LOT")
        );
        String direction = SafeMapAccessUtil.getString(cond, "DIRECTION");
        Object includeRootRaw = SafeMapAccessUtil.getBoolean(cond, "INCLUDE_ROOT");
        boolean includeRoot = toBoolean(includeRootRaw, true);

        // --- 入力バリデーション（null/空/フォーマット）
        if (StringCheckUtil.isNullOrEmpty(startLot)) {
            return EventResultDto.error("START_LOT が未指定です。");
        }
        if (StringCheckUtil.isNullOrEmpty(direction)) {
            return EventResultDto.error("DIRECTION が未指定です。");
        }
        direction = direction.trim().toUpperCase(Locale.ROOT);
        if (!("F".equals(direction) || "B".equals(direction))) {
            return EventResultDto.error("DIRECTION は 'F' または 'B' を指定してください。");
        }

        // --- Connection 取得（外部供給を優先／無ければ pietoro で自前接続）
        Connection useConn = this.conn;
        boolean createdHere = false;
        if (useConn == null) {
            try {
                useConn = DbConnections.open(COMPANY_CD);
                createdHere = true;
            } catch (SQLException e) {
                logInfo("DB接続に失敗: " + e.getMessage());
                return EventResultDto.error("DB接続に失敗しました。詳細: " + e.getMessage());
            }
        }

        // --- REQUEST_ID は Java 側で採番（NULL 明示渡しを回避）
        UUID requestIdObj = UUID.randomUUID();
        List<Object> inParams = Arrays.asList(startLot, direction, requestIdObj, includeRoot);

        try {
            Map<String, Object> result = StoredProcUtil.callProcedure(useConn, PROC_NAME, inParams, null);

            // --- 結果セット安全取得（null/空/キー欠落に耐性）
            String requestId = requestIdObj.toString();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (result != null)
                    ? (List<Map<String, Object>>) result.get("resultSet")
                    : null;

            if (rows != null && !rows.isEmpty()) {
                Map<String, Object> first = rows.get(0);
                if (first != null) {
                    Object rid = first.get("REQUEST_ID");
                    if (rid != null && !rid.toString().trim().isEmpty()) {
                        requestId = rid.toString();
                    }
                }
            }

            logInfo("SP 実行完了: proc=" + PROC_NAME
                    + ", startLot=" + startLot
                    + ", direction=" + direction
                    + ", includeRoot=" + includeRoot
                    + ", REQUEST_ID=" + requestId);

            // TODO: EventResultDto にデータ格納用のAPIがあれば requestId を詰める
            // 例）return EventResultDto.ok().withData("REQUEST_ID", requestId);
            return EventResultDto.ok();

        } catch (SQLException e) {
            logInfo("SP 実行に失敗: " + e.getMessage());
            return EventResultDto.error("BOM作成処理に失敗しました。詳細: " + e.getMessage());
        } catch (RuntimeException e) {
            // NPE 等のランタイム例外も握って明示化
            logInfo("SP 実行中に予期せぬエラー: " + e.getMessage());
            return EventResultDto.error("BOM作成処理で予期せぬエラーが発生しました。");
        } finally {
            if (createdHere) {
                DbConnections.closeQuietly(useConn); // 自前取得のみクローズ
            }
        }
    }

    // ===== ユーティリティ（このクラス内限定） =====

    private static String coalesce(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isEmpty()) return v;
        }
        return null;
    }

    /**
     * Object を boolean に安全変換。null/空は defaultVal。
     * 許容: true/false, 1/0, "true"/"false", "t"/"f", "yes"/"no", "y"/"n"
     */
    private static boolean toBoolean(Object v, boolean defaultVal) {
        if (v == null) return defaultVal;
        if (v instanceof Boolean) return (Boolean) v;
        String s = v.toString().trim();
        if (s.isEmpty()) return defaultVal;
        String ls = s.toLowerCase(Locale.ROOT);
        if ("1".equals(ls) || "true".equals(ls) || "t".equals(ls) || "yes".equals(ls) || "y".equals(ls)) return true;
        if ("0".equals(ls) || "false".equals(ls) || "f".equals(ls) || "no".equals(ls)  || "n".equals(ls)) return false;
        return defaultVal;
    }
}
