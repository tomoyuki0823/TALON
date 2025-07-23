
package jp.co.technopro.talon.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DbConfigLoader {

    private static final String DEFAULT_PATH = "config/db.properties";

    public static DbConfig load() {
        return load(DEFAULT_PATH);
    }

    public static DbConfig load(String path) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("DB設定ファイルの読み込みに失敗しました: " + path, e);
        }

        DbConfig config = new DbConfig();
        config.setUrl(props.getProperty("url"));
        config.setUser(props.getProperty("user"));
        config.setPassword(props.getProperty("password"));
        config.setDriver(props.getProperty("driver"));
        return config;
    }
}
