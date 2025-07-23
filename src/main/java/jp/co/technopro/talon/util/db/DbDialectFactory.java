package jp.co.technopro.talon.util.db;

import java.sql.Connection;
import java.sql.SQLException;

public class DbDialectFactory {

    /**
     * 接続情報から適切なDialectを返却します。
     *
     * @param conn JDBCコネクション
     * @return 対応するDbDialect実装
     */
    public static DbDialect createDialect(Connection conn) throws SQLException {
        String productName = conn.getMetaData().getDatabaseProductName();

        switch (productName) {
            case "PostgreSQL":
                return new PostgreSqlDialect();
            case "Microsoft SQL Server":
                return new SqlServerDialect();  // 実装必要
            case "MySQL":
                return new MySqlDialect();      // 実装必要
            case "Oracle":
                return new OracleDialect();     // 実装必要
            default:
                throw new UnsupportedOperationException("未対応のDB製品: " + productName);
        }
    }
}

