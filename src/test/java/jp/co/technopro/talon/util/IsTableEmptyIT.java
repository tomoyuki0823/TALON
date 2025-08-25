package jp.co.technopro.talon.util;

import jp.co.technopro.talon.db.common.DbConfig;
import jp.co.technopro.talon.db.common.DbConfigLoader;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static jp.co.technopro.talon.util.common.DbUtil.isTableEmpty;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 実 DB に対する isTableEmpty の振る舞いを検証する統合テスト。
 *
 * <p>前提:
 * <ul>
 *   <li>会社コードごとの DB 設定ファイルがクラスパス or 外部に存在（例: {@code db_{company}.properties}）。</li>
 *   <li>テーブル {@code TK_YOTAKU}（例）に列 {@code COMPANY_CD, TK_NO, SHORI_TUKI} が存在。</li>
 * </ul>
 * 列名やテーブル名が異なる場合は、定数をあなたのスキーマに合わせて変更してください。
 * </p>
 */
public class IsTableEmptyIT {

    private static final String COMPANY_CD = "gojo";             // ★あなたの会社コードに合わせる
    private static final String TABLE_TK_YOTAKU = "TK_YOTAKU";  // ★実テーブル名に合わせる
    private static final String COL_COMPANY = "COMPANY_CD";     // ★会社コード列
    private static final String COL_TK_NO   = "TK_NO";          // ★得意先? or 伝票No 等
    private static final String COL_SHORI   = "SHORI_TUKI";     // ★処理月(YYYYMM)

    private static Connection conn;

    @BeforeAll
    static void open() throws Exception {
        conn = getConnection(COMPANY_CD);
        conn.setAutoCommit(false); // テストデータはロールバック前提
    }

    @AfterAll
    static void close() throws Exception {
        if (conn != null) conn.close();
    }

    @AfterEach
    void rollback() throws Exception {
        if (conn != null) conn.rollback();
    }

    /**
     * レコードが存在しない条件 → isTableEmpty は true を返すこと。
     */
    @Test
    void testIsTableEmpty_whenNoRow_shouldReturnTrue() throws Exception {
        // unique な値にして衝突を避ける
        String tkNo = "99999";
        String shori = "202506";

        Map<String, Object> where = where(tkNo, shori);

        boolean actual = isTableEmpty(conn, TABLE_TK_YOTAKU, where, COMPANY_CD);
        assertTrue(actual, "該当0件のはずなので true（空）を期待");
    }

    /**
     * レコードが存在する条件 → isTableEmpty は false を返すこと。
     */
    @Test
    void testIsTableEmpty_whenExists_shouldReturnFalse() throws Exception {
        String tkNo = "99999";
        String shori = "209901";

        // 先に1件だけ作る
        insertYotakuRow(COMPANY_CD, tkNo, shori);

        Map<String, Object> where = where(tkNo, shori);

        boolean actual = isTableEmpty(conn, TABLE_TK_YOTAKU, where, COMPANY_CD);
        assertFalse(actual, "該当1件あるので false（空ではない）を期待");
    }

    // ----------------- helpers -----------------

    private static Map<String, Object> where(String tkNo, String shoriTuki) {
        Map<String, Object> where = new HashMap<>();
        where.put(COL_TK_NO, tkNo);
        where.put(COL_SHORI, shoriTuki);
        // companyCd はメソッド第4引数で渡す前提（スキーマに company を物理列で持たない場合でもOK）
        return where;
    }

    /**
     * テストデータ投入。列名/型は実スキーマに合わせて調整してください。
     */
    private static void insertYotakuRow(String companyCd, String tkNo, String shoriTuki) throws SQLException {
        String sql =
                "INSERT INTO " + TABLE_TK_YOTAKU + " (" + COL_TK_NO + "," + COL_SHORI + ") " +
                        "VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tkNo);
            ps.setString(2, shoriTuki);
            ps.executeUpdate();
        }
    }

    /**
     * DbConfigLoader を使って実 DB のコネクションを取得。
     * DataSource があればそれを優先。無ければ DriverManager で作成。
     */
    private static Connection getConnection(String companyCd) throws Exception {
        DbConfig cfg = DbConfigLoader.load(companyCd);
        if (cfg.getDataSource() != null) {
            return cfg.getDataSource().getConnection();
        }
        // 明示ドライバ（必要に応じて）
        if (cfg.getDriver() != null && !cfg.getDriver().isEmpty()) {
            Class.forName(cfg.getDriver());
        }
        return DriverManager.getConnection(cfg.getUrl(), cfg.getUser(), cfg.getPassword());
    }
}
