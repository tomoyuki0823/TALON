package jp.co.technopro.talon.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbConfigLoader {

    private static final String DEFAULT_CLASSPATH = "config/db.properties";

    public static DbConfig load() {
        return loadFromClasspath(DEFAULT_CLASSPATH);
    }

    private static DbConfig loadFromClasspath(String resourcePath) {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new RuntimeException("クラスパス上のDB設定ファイルが見つかりません: " + resourcePath);
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("DB設定ファイルの読み込みに失敗しました: " + resourcePath, e);
        }

        DbConfig config = new DbConfig();
        config.setUrl(props.getProperty("url"));
        config.setUser(props.getProperty("user"));
        config.setPassword(props.getProperty("password"));
        config.setDriver(props.getProperty("driver"));
        return config;
    }
}
