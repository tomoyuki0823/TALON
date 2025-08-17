package jp.co.technopro.talon.logic.pietoro;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;
import jp.co.technopro.talon.util.common.StringCheckUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

import static jp.co.technopro.talon.util.common.StoredProcUtil.callProcedure;

/**
 * PSI: 編集差分を保存し、必要範囲の在庫を再計算するロジック。
 *
 * <h3>想定入力（paramDto.conditionData）</h3>
 * <ul>
 *   <li>REQUEST_ID : String (UUID)</li>
 *   <li>USER_CD    : String</li>
 *   <li>ITEM_CD    : String（再計算対象の品目。未指定なら null）</li>
 *   <li>FROM       : String (yyyy-MM-dd)</li>
 *   <li>TO         : String (yyyy-MM-dd)</li>
 *   <li>EDITS      : List&lt;Map&gt;  // 例: [{ITEM_CD, MEASURE, DATE, QTY}, ...]</li>
 *   <li>または EDITS_JSON : String // 上記配列JSONを文字列で（DBにそのまま渡す運用）</li>
 * </ul>
 *
 * <h3>処理</h3>
 * <ol>
 *   <li>差分の物理反映（SP_APPLY_PROC_EDITS）</li>
 *   <li>在庫の再計算（SP_RECALC_STOCK）</li>
 * </ol>
 */
public class PsiSaveEditsLogic extends AbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {

        Map<String, Object> cond = (paramDto != null) ? paramDto.getConditionData() : null;
        if (cond == null) {
            logInfo("条件が null です。");
            return EventResultDto.error("条件データが未設定です。");
        }

        // 必須
        String ridStr = SafeMapAccessUtil.getString(cond, "REQUEST_ID");
        String user = SafeMapAccessUtil.getString(cond, "USER_CD");
        String fromS = SafeMapAccessUtil.getString(cond, "FROM");
        String toS = SafeMapAccessUtil.getString(cond, "TO");

        if (StringCheckUtil.isNullOrEmpty(ridStr)) return EventResultDto.error("REQUEST_ID が未指定です。");
        if (StringCheckUtil.isNullOrEmpty(user)) return EventResultDto.error("USER_CD が未指定です。");
        if (StringCheckUtil.isNullOrEmpty(fromS) || StringCheckUtil.isNullOrEmpty(toS)) {
            return EventResultDto.error("FROM/TO（再計算範囲）が未指定です。");
        }

        UUID rid;
        LocalDate from, to;
        try {
            rid = UUID.fromString(ridStr.trim());
            from = LocalDate.parse(fromS.trim());
            to = LocalDate.parse(toS.trim());
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            logInfo("ID/日付のパースに失敗: " + ex.getMessage());
            return EventResultDto.error("REQUEST_ID または日付形式が不正です。");
        }

        // 任意
        String item = StringCheckUtil.isNullOrEmpty(SafeMapAccessUtil.getString(cond, "ITEM_CD"))
                ? null : SafeMapAccessUtil.getString(cond, "ITEM_CD").trim();

        // 差分の受け取り（EDITS または EDITS_JSON）
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> editsMapList = (List<Map<String, Object>>) cond.get("EDITS");
        String editsJson = SafeMapAccessUtil.getString(cond, "EDITS_JSON");

        // どちらも無い／空なら何もしない（成功扱いで返してOK）
        if ((editsMapList == null || editsMapList.isEmpty())
                && StringCheckUtil.isNullOrEmpty(editsJson)) {
            logInfo("編集差分なし：在庫再計算のみ実行します。");
        }

        try {
            PsiService svc = new PsiService(this.conn); // 既存コネクションを渡す（外部管理）

            // 1) 差分の物理反映
            if (editsMapList != null && !editsMapList.isEmpty()) {
                // 画面から配列で来るパターン
                List<PsiService.PsiEditCell> cells = toEditCells(editsMapList);
                if (cells.isEmpty()) {
                    logInfo("有効な編集差分が抽出できませんでした。");
                } else {
                    svc.applyEdits(rid, cells, user);
                }
            } else if (!StringCheckUtil.isNullOrEmpty(editsJson)) {
                // JSON文字列で来るパターン（そのままSPに渡したい場合は直叩きでもOK）
                callProcedure(this.conn, "dbo.SP_APPLY_PROC_EDITS",
                        Arrays.asList(rid, editsJson, user), null);
            }

            // 2) 在庫を範囲再計算
            svc.recalcStock(rid, item, from, to);

            logInfo("PSI 保存/再計算 完了: RID=" + rid + ", item=" + item + ", range=" + from + ".." + to);
            return EventResultDto.ok();

        } catch (SQLException e) {
            logInfo("保存/再計算に失敗（SQL）: " + e.getMessage());
            return EventResultDto.error("保存に失敗しました。詳細: " + e.getMessage());
        } catch (Exception e) {
            logInfo("保存/再計算に失敗: " + e.getMessage());
            return EventResultDto.error("保存に失敗しました。");
        }
    }

    // ===== 画面の EDITS(List<Map>) を PsiEditCell リストへ変換 =====
    private static List<PsiService.PsiEditCell> toEditCells(List<Map<String, Object>> edits) {
        List<PsiService.PsiEditCell> out = new ArrayList<>();
        if (edits == null) return out;

        for (Map<String, Object> m : edits) {
            if (m == null || m.isEmpty()) continue;

            String item = str(m.get("ITEM_CD"));
            String measure = str(m.get("MEASURE"));
            String dateS = str(m.get("DATE"));
            BigDecimal qty = dec(m.get("QTY"));

            if (StringCheckUtil.isNullOrEmpty(item)
                    || StringCheckUtil.isNullOrEmpty(measure)
                    || StringCheckUtil.isNullOrEmpty(dateS)) {
                continue; // 必須欠落はスキップ
            }
            LocalDate d;
            try {
                d = LocalDate.parse(dateS.trim());
            } catch (Exception ex) {
                continue; // 不正日付はスキップ
            }
            out.add(new PsiService.PsiEditCell(item.trim(), measure.trim().toUpperCase(Locale.ROOT), d, qty));
        }
        return out;
    }

    // ===== 小ユーティリティ =====
    private static String str(Object o) {
        return (o == null) ? null : o.toString();
    }

    private static BigDecimal dec(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        String s = o.toString().trim();
        if (s.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
