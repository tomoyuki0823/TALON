package jp.co.technopro.talon.util.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * テスト用 DB 接続ユーティリティ。
 * 実環境と切り離して単体テスト実行可能とする。
 */
public class DbTestUtil {

    /**
     * テスト用のJDBC接続を取得します。
     * @return Connection
     */
    public static Connection getTestConnection() {
        try {
            String url = "jdbc:sqlserver://172.31.6.72:1433;DatabaseName=TALON;encrypt=false";
            String user = "sa";
            String password = "knight"; // 適切に設定してください
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("テスト用DB接続に失敗しました", e);
        }
    }
}