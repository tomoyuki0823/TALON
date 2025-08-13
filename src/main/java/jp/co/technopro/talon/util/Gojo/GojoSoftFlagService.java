package jp.co.technopro.talon.util.Gojo;

import java.sql.Connection;
import java.util.Map;
import java.util.Objects;

/**
 * DBのフラグ列を「静かに」更新するユーティリティ（ログ出力なし・例外外だしなし）。
 * <p>トランザクション（commit/rollback）は呼び出し側で管理してください。</p>
 */
public final class GojoSoftFlagService {

    private GojoSoftFlagService() {}

    /**
     * フラグ更新を静かに実行します。失敗しても例外を外に投げません（完全握りつぶし）。
     *
     * @param conn    既存コネクション（必須／Txは呼び出し側で管理）
     * @param table   テーブル名（例：TABLE_TK_IRYO）
     * @param flagCol フラグ列（例：KAIIN_HIKAIIN_FLG）
     * @param flagVal 設定値（例："1"）
     * @param where   WHERE 条件（列名→値）。空や null の場合は何もせず戻ります。
     */
    public static void updateFlagSilently(
            Connection conn,
            String table,
            String flagCol,
            Object flagVal,
            Map<String, Object> where
    ) {
        try {
            if (where == null || where.isEmpty()) return; // 安全のため無視
            Objects.requireNonNull(conn, "conn");
            Objects.requireNonNull(table, "table");
            Objects.requireNonNull(flagCol, "flagCol");
            GojoDbFlagUtil.updateFlag(conn, table, flagCol, flagVal, where);
        } catch (Exception ignore) {
            // 何もしない（ログも残さない）
        }
    }
}
