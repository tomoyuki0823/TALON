package jp.co.technopro.talon.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbTestUtil {
    public static Connection getTestConnection() throws SQLException, ClassNotFoundException {
        String url = "jdbc:sqlserver://172.31.6.72:1433;DatabaseName=TALON;encrypt=false;useSSL=false";
        String user = "sa";
        String password = "knight";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(url, user, password);
    }
}
