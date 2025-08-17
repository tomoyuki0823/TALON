package jp.co.technopro.talon.util.common;

import java.sql.*;
import java.util.*;

/**
 * ストアドプロシージャを汎用的に呼び出すユーティリティ。
 * <p>
 * SQL Server / JDBC CallableStatement を利用して、
 * 入出力パラメータや戻り値を含めた実行結果を取得します。
 */
public class StoredProcUtil {

    private StoredProcUtil() {
        // インスタンス化禁止
    }

    /**
     * ストアドプロシージャを呼び出し、結果をマップで返します。
     *
     * @param conn       DBコネクション
     * @param procName   ストアドプロシージャ名（例: "sp_sample_proc"）
     * @param inParams   入力パラメータ（順番に対応）
     * @param outParams  出力パラメータの JDBC 型（順番に対応）
     * @return 実行結果マップ（キー: param1, param2... / resultSet / returnValue）
     * @throws SQLException SQL実行時の例外
     */
    public static Map<String, Object> callProcedure(
            Connection conn, String procName,
            List<Object> inParams, List<Integer> outParams) throws SQLException {

        if (conn == null) {
            throw new IllegalArgumentException("Connection must not be null");
        }
        if (procName == null || procName.isEmpty()) {
            throw new IllegalArgumentException("procName must not be blank");
        }

        List<Object> ins  = (inParams  != null) ? inParams  : Collections.emptyList();
        List<Integer> outs = (outParams != null) ? outParams : Collections.emptyList();

        // {? = call proc(?,?,?...)} を生成（戻り値 + IN 個数 + OUT 個数）
        int paramCount = ins.size() + outs.size();
        StringBuilder sb = new StringBuilder("{? = call ");
        sb.append(procName).append("(");
        for (int i = 0; i < paramCount; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        sb.append(")}");
        String callSql = sb.toString();

        Map<String, Object> resultMap = new HashMap<>();

        try (CallableStatement cs = conn.prepareCall(callSql)) {
            int index = 1;

            // 戻り値（SQL Server の return 値）
            cs.registerOutParameter(index++, Types.INTEGER);

            // IN パラメータ設定（Boolean/UUID は明示対応。null は setNull）
            for (Object v : ins) {
                if (v == null) {
                    cs.setNull(index++, Types.NULL);
                } else if (v instanceof Boolean) {
                    cs.setBoolean(index++, (Boolean) v);
                } else if (v instanceof java.util.UUID) {
                    cs.setObject(index++, v); // mssql-jdbc は UUID を受け付けます
                } else {
                    cs.setObject(index++, v);
                }
            }

            // OUT パラメータがあれば登録
            for (Integer sqlType : outs) {
                cs.registerOutParameter(index++, sqlType);
            }

            boolean hasRs = cs.execute();

            // return 値
            resultMap.put("returnValue", cs.getObject(1));

            // OUT 値の回収（param1..）
            for (int i = 0; i < outs.size(); i++) {
                // 戻り値(1) + IN 個数(ins.size()) + i(0-based) なので +2 から
                resultMap.put("param" + (i + 1), cs.getObject(ins.size() + 2 + i));
            }

            // 結果セット（最初のものだけ取得）
            if (hasRs) {
                try (ResultSet rs = cs.getResultSet()) {
                    List<Map<String, Object>> rows = new ArrayList<>();
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int c = 1; c <= colCount; c++) {
                            row.put(meta.getColumnLabel(c), rs.getObject(c));
                        }
                        rows.add(row);
                    }
                    resultMap.put("resultSet", rows);
                }
            }
        }
        return resultMap;
    }

}
