package jp.co.technopro.talon.util.Gojo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 単純な「テーブルのフラグ列を更新する」ための JDBC ユーティリティ。
 * <p>
 * 注意：
 * <ul>
 *   <li>テーブル名・列名はアプリ側の定数のみを渡してください（動的入力は渡さない）</li>
 *   <li>値バインドは PreparedStatement で行うため SQL インジェクション対策は確保されます</li>
 *   <li>トランザクション（commit/rollback）は呼び出し側で制御してください</li>
 * </ul>
 */
public final class GojoDbFlagUtil {

    private GojoDbFlagUtil() {}

    /**
     * 指定テーブルのフラグ列を指定値に更新します。WHERE は渡されたマップの全キー一致。
     *
     * @param conn     既存コネクション（呼び出し側でトランザクション管理）
     * @param table    テーブル名（例：TK_IRYO）
     * @param flagCol  更新対象のフラグ列名（例：RYOYO_YM_JIKO_FLG）
     * @param flagVal  フラグ値（例："1"）
     * @param whereEq  WHERE 条件（列名→値）。AND で連結されます。空/NULLは全件更新を避けるため例外。
     * @return 更新件数
     * @throws SQLException SQL実行時の例外
     * @throws IllegalArgumentException WHEREが空の場合
     */
    public static int updateFlag(Connection conn,
                                 String table,
                                 String flagCol,
                                 Object flagVal,
                                 Map<String, Object> whereEq) throws SQLException {
        if (whereEq == null || whereEq.isEmpty()) {
            throw new IllegalArgumentException("WHERE 条件が空です（安全のため全件更新は禁止）");
        }

        // UPDATE {table} SET {flagCol} = ? WHERE col1 = ? AND col2 = ? ...
        StringJoiner where = new StringJoiner(" AND ");
        for (String col : whereEq.keySet()) {
            where.add(col + " = ?");
        }
        String sql = "UPDATE " + table + " SET " + flagCol + " = ? WHERE " + where;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setObject(idx++, flagVal);
            for (Object v : whereEq.values()) {
                ps.setObject(idx++, v);
            }
            return ps.executeUpdate();
        }
    }
}
