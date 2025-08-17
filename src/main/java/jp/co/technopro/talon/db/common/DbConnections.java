package jp.co.technopro.talon.db.common;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 会社コードをキーに DB 接続を開く/閉じるユーティリティ。
 * <p>JNDI, 外部ファイル, クラスパスの順に {@link DbConfigLoader} が設定を決定。</p>
 */
public final class DbConnections {

    private DbConnections() {}

    /**
     * CompanyCd を指定して新規に Connection を開く。
     * <p>DataSource があればそれを優先。なければ DriverManager で接続。</p>
     */
    public static Connection open(String companyCd) throws SQLException {
        DbConfig cfg = DbConfigLoader.load(companyCd);

        DataSource ds = cfg.getDataSource();
        if (ds != null) {
            return ds.getConnection();
        }

        // DataSource が無い場合は直結
        try {
            if (cfg.getDriver() != null && !cfg.getDriver().isBlank()) {
                Class.forName(cfg.getDriver());
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBCドライバのロードに失敗しました: " + cfg.getDriver(), e);
        }
        return DriverManager.getConnection(cfg.getUrl(), cfg.getUser(), cfg.getPassword());
    }

    /** Java側で取得した Connection を静かにクローズ（null可） */
    public static void closeQuietly(Connection c) {
        if (c != null) {
            try { c.close(); } catch (SQLException ignore) {}
        }
    }
}
