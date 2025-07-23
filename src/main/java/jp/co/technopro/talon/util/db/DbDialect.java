package jp.co.technopro.talon.util.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DbDialect {
    /**
     * 指定されたテーブルの実カラム名一覧を返す（valueMapに含まれるカラムだけ）
     */
    List<String> getTableColumns(Connection conn, String tableName, Map<String, Object> valueMap) throws SQLException;

    /**
     * この方言のDB製品名（例: "PostgreSQL"）
     */
    String getProductName();
}

