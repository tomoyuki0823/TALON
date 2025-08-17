package jp.co.technopro.talon.logic.pietoro;

import jp.co.technopro.talon.db.common.DbConnections;
import jp.co.technopro.talon.util.common.StoredProcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** PSI横持ち（TALON画面）用のオーケストレーション・サービス（ハイブリッド）。 */
public class PsiService {

    private static final String COMPANY_CD = "pietoro";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    /** 外部から渡されたコネクション（あればこれを使う／クローズは呼び出し側） */
    private final Connection externalConn;

    /** 外部コネクションなし（必要時に自前で開閉） */
    public PsiService() { this.externalConn = null; }

    /** 外部コネクションあり（※このサービスではクローズしません） */
    public PsiService(Connection conn) { this.externalConn = conn; }

    /** 小さなテンプレ：外部connがあればそれを使い、無ければ自前でopen/close */
    private interface SqlWork<T> { T run(Connection c) throws SQLException; }
    private <T> T withConn(SqlWork<T> work) throws SQLException {
        Connection c = externalConn;
        boolean createdHere = false;
        if (c == null) {
            c = DbConnections.open(COMPANY_CD);
            createdHere = true;
        }
        try {
            return work.run(c);
        } finally {
            if (createdHere) {
                DbConnections.closeQuietly(c);
            }
        }
    }

    /** 1) 物理展開（RID採番→期間/条件でT_PROC_*を詰める） */
    public UUID begin(YearMonth ym, String itemCd, String user) throws SQLException {
        UUID rid = UUID.randomUUID();
        LocalDate from = ym.atDay(1), to = ym.atEndOfMonth();
        withConn(c -> {
            StoredProcUtil.callProcedure(c, "dbo.SP_PROC_REQUEST_BEGIN",
                    Arrays.asList(rid, from.toString(), to.toString(), itemCd, user), null);
            return null;
        });
        return rid;
    }

    /** 2) 画面データ取得（物理表→PIVOTした横持ち） */
    public PsiGridDto getGrid(UUID rid, LocalDate from, LocalDate to) throws SQLException {
        return withConn(c -> {
            Map<String, Object> res = StoredProcUtil.callProcedure(c, "dbo.SP_GET_PROC_PLAN_GRID",
                    Arrays.asList(rid, from.toString(), to.toString()), null);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) res.get("resultSet");
            if (rows == null) rows = Collections.emptyList();

            // ヘッダ検出（d_yyyyMMdd）
            SortedMap<LocalDate, String> dayKeyByDate = new TreeMap<>();
            if (!rows.isEmpty()) {
                for (String key : rows.get(0).keySet()) {
                    if (key != null && key.startsWith("d_") && key.length() == 10) {
                        String ymd = key.substring(2);
                        LocalDate d = LocalDate.of(
                                Integer.parseInt(ymd.substring(0,4)),
                                Integer.parseInt(ymd.substring(4,6)),
                                Integer.parseInt(ymd.substring(6,8)));
                        dayKeyByDate.put(d, key);
                    }
                }
            }
            List<PsiDayHeader> headers = new ArrayList<>();
            for (LocalDate d : dayKeyByDate.keySet()) headers.add(new PsiDayHeader(d));

            // 行変換
            List<PsiRowDto> dtoRows = new ArrayList<>();
            for (Map<String,Object> r : rows) {
                PsiRowDto dto = new PsiRowDto();
                dto.itemCd   = str(r.get("ITEM_CD"));
                dto.itemName = str(r.get("ITEM_NAME"));
                dto.rowKind  = str(r.get("ROW_KIND")); // 納入/発注/受注/在庫
                dto.prevDay  = dec(r.get("PREV_DAY"));
                dto.values   = new LinkedHashMap<>();
                for (Map.Entry<LocalDate,String> e : dayKeyByDate.entrySet()) {
                    dto.values.put(e.getKey(), dec(r.get(e.getValue())));
                }
                dtoRows.add(dto);
            }
            PsiGridDto dto = new PsiGridDto();
            dto.from = from; dto.to = to; dto.headers = headers; dto.rows = dtoRows;
            return dto;
        });
    }

    /** 3) 差分反映（JSON配列をSPに渡す）→必要なら在庫再計算へ */
    public void applyEdits(UUID rid, List<PsiEditCell> edits, String user) throws SQLException {
        String json = toJson(edits); // Jackson等に置換可
        withConn(c -> {
            StoredProcUtil.callProcedure(c, "dbo.SP_APPLY_PROC_EDITS",
                    Arrays.asList(rid, json, user), null);
            return null;
        });
    }

    /** 4) 在庫再計算（範囲/アイテム指定可） */
    public void recalcStock(UUID rid, String itemCd, LocalDate from, LocalDate to) throws SQLException {
        withConn(c -> {
            StoredProcUtil.callProcedure(c, "dbo.SP_RECALC_STOCK",
                    Arrays.asList(rid,
                            nullIfBlank(itemCd),
                            from != null ? from.toString() : null,
                            to   != null ? to.toString()   : null),
                    null);
            return null;
        });
    }

    /** 5) 確定（業務テーブルへ転記） */
    public void commit(UUID rid, String user) throws SQLException {
        withConn(c -> {
            StoredProcUtil.callProcedure(c, "dbo.SP_PROC_REQUEST_COMMIT",
                    Arrays.asList(rid, user), null);
            return null;
        });
    }

    // ===== DTO =====
    public static class PsiGridDto {
        public LocalDate from, to;
        public List<PsiDayHeader> headers;
        public List<PsiRowDto> rows;
    }
    public static class PsiDayHeader {
        public final LocalDate date;
        public PsiDayHeader(LocalDate d){ this.date=d; }
        public String label(){ return String.format("%02d/%02d", date.getMonthValue(), date.getDayOfMonth()); }
    }
    public static class PsiRowDto {
        public String itemCd, itemName, rowKind; // 納入/発注/受注/在庫
        public BigDecimal prevDay;
        public Map<LocalDate, BigDecimal> values; // 日付→数量
    }
    /** measure: 'NOUNYU'|'PO'|'JUCHU'（在庫は編集不可） */
    public static class PsiEditCell {
        public String itemCd, measure; public LocalDate date; public BigDecimal qty;
        public PsiEditCell(String itemCd,String measure,LocalDate date,BigDecimal qty){
            this.itemCd=itemCd; this.measure=measure; this.date=date; this.qty=qty;
        }
    }

    // ===== 小ユーティリティ =====
    private static String str(Object o){ return o==null? null : o.toString(); }
    private static BigDecimal dec(Object o){
        if (o==null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal)o;
        if (o instanceof Number)     return BigDecimal.valueOf(((Number)o).doubleValue());
        String s=o.toString().trim(); if (s.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(s); } catch(Exception e){ return BigDecimal.ZERO; }
    }
    private static String nullIfBlank(String s){ return (s==null||s.trim().isEmpty())? null : s.trim(); }
    private static String toJson(List<PsiEditCell> edits){
        StringBuilder sb=new StringBuilder("[");
        for(int i=0;i<edits.size();i++){
            PsiEditCell e=edits.get(i);
            sb.append("{\"itemCd\":\"").append(esc(e.itemCd)).append("\",")
                    .append("\"measure\":\"").append(esc(e.measure)).append("\",")
                    .append("\"date\":\"").append(e.date.format(ISO)).append("\",")
                    .append("\"qty\":").append(e.qty==null?"0":e.qty.toPlainString()).append("}");
            if(i<edits.size()-1) sb.append(",");
        }
        return sb.append("]").toString();
    }
    private static String esc(String s){ return s==null? "" : s.replace("\\","\\\\").replace("\"","\\\""); }
}
