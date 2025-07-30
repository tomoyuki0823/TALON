package jp.co.technopro.talon.db.common;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * DB設定ローダー。
 * <p>
 * 以下の優先順位でDB設定を読み込みます：
 * <ol>
 *     <li>JNDIリソース（例: jdbc/TalonDb）</li>
 *     <li>外部プロパティファイル（-Ddb.config or 環境変数 DB_CONFIG）</li>
 *     <li>クラスパス上の db.properties</li>
 * </ol>
 */
public class DbConfigLoader {

    private static final String DEFAULT_CLASSPATH = "db.properties";
    private static final String DEFAULT_JNDI_NAME = "jdbc/TalonDb";

    /**
     * DB設定をロードします。
     *
     * @return DbConfig インスタンス
     */
    public static DbConfig load(String companyCd) {
        // 1. JNDI（共通化のため JNDI は companyCd で分岐しない前提）
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(DEFAULT_JNDI_NAME);
            DbConfig config = new DbConfig();
            config.setDataSource(ds);
            return config;
        } catch (NamingException ignore) {}

        // 2. 外部ファイル（companyCd付き）
        String externalPath = System.getProperty("db.config");
        if (externalPath == null || externalPath.isEmpty()) {
            externalPath = System.getenv("DB_CONFIG");
        }

        if (externalPath != null && !externalPath.isEmpty()) {
            // パスがディレクトリの場合は companyCd を反映
            if (!externalPath.endsWith(".properties")) {
                externalPath = externalPath + "/db_" + companyCd.toLowerCase() + ".properties";
            }
            return loadFromFile(externalPath);
        }

        // 3. クラスパス（companyCd付きに変更）
        String resourcePath = "db_" + companyCd.toLowerCase() + ".properties";
        return loadFromClasspath(resourcePath);
    }

    /**
     * クラスパス上のプロパティファイルを読み込みます。
     */
    public static DbConfig loadFromClasspath(String resourcePath) {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new RuntimeException("クラスパス上のDB設定ファイルが見つかりません: " + resourcePath);
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("DB設定ファイルの読み込みに失敗しました: " + resourcePath, e);
        }
        return fromProperties(props);
    }

    /**
     * 外部ファイル（絶対パス）を読み込みます。
     */
    public static DbConfig loadFromFile(String filePath) {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(filePath)) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("外部DB設定ファイルの読み込みに失敗しました: " + filePath, e);
        }
        return fromProperties(props);
    }

    /**
     * プロパティからDbConfigを構築します。
     */
    private static DbConfig fromProperties(Properties props) {
        DbConfig config = new DbConfig();
        config.setUrl(props.getProperty("url"));
        config.setUser(props.getProperty("user"));
        config.setPassword(props.getProperty("password"));
        config.setDriver(props.getProperty("driver"));
        return config;
    }
}
